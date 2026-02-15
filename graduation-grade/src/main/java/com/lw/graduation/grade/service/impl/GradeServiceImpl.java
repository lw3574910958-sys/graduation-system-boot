package com.lw.graduation.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.grade.GradeCreateDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeUpdateDTO;
import com.lw.graduation.api.service.grade.GradeService;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 成绩服务实现类
 * 实现成绩管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GradeServiceImpl extends ServiceImpl<BizGradeMapper, BizGrade> implements GradeService {

    private final BizGradeMapper bizGradeMapper;
    private final BizStudentMapper bizStudentMapper;
    private final BizTopicMapper bizTopicMapper;
    private final BizSelectionMapper bizSelectionMapper;
    private final BizTeacherMapper bizTeacherMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询成绩列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<GradeVO> getGradePage(GradePageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStudentId() != null, BizGrade::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizGrade::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getGraderId() != null, BizGrade::getGraderId, queryDTO.getGraderId())
                .ge(queryDTO.getMinScore() != null, BizGrade::getScore, queryDTO.getMinScore())
                .le(queryDTO.getMaxScore() != null, BizGrade::getScore, queryDTO.getMaxScore())
                .orderByDesc(BizGrade::getGradedAt); // 按评分时间倒序

        // 2. 执行分页查询
        IPage<BizGrade> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizGrade> gradePage = bizGradeMapper.selectPage(page, wrapper);

        // 3. 转换为VO并补充关联信息
        IPage<GradeVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(gradePage.getRecords().stream()
                .map(this::convertToGradeVO)
                .collect(Collectors.toList()));
        voPage.setTotal(gradePage.getTotal());

        return voPage;
    }

    /**
     * 根据ID获取成绩详情（带缓存穿透防护）
     *
     * @param id 成绩ID
     * @return 成绩详情
     */
    @Override
    public GradeVO getGradeById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.GRADE_INFO + id;
        
        // 1. 查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (CacheConstants.CacheValue.NULL_MARKER.equals(cached)) {
                log.debug("缓存命中空值标记，成绩不存在: " + id);
                return null;
            }
            return (GradeVO) cached;
        }

        // 2. 缓存未命中，查数据库
        BizGrade grade = bizGradeMapper.selectById(id);
        if (grade == null) {
            // 缓存空值防止穿透
            redisTemplate.opsForValue().set(
                cacheKey,
                CacheConstants.CacheValue.NULL_MARKER,
                CacheConstants.CacheValue.NULL_EXPIRE,
                TimeUnit.SECONDS
            );
            log.debug("成绩不存在，缓存空值标记: " + cacheKey);
            return null;
        }

        // 3. 转换并缓存结果
        GradeVO result = convertToGradeVO(grade);
        redisTemplate.opsForValue().set(
            cacheKey,
            result,
            CacheConstants.ExpireTime.GRADE_INFO_EXPIRE,
            TimeUnit.SECONDS
        );
        log.debug("缓存成绩信息: " + cacheKey);
        return result;
    }

    /**
     * 创建成绩
     *
     * @param createDTO 创建参数
     */
    @Override
    @Transactional
    public void createGrade(GradeCreateDTO createDTO) {
        // 1. 验证学生是否存在
        BizStudent student = bizStudentMapper.selectById(createDTO.getStudentId());
        if (student == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "学生不存在");
        }

        // 2. 验证课题是否存在
        BizTopic topic = bizTopicMapper.selectById(createDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "课题不存在");
        }

        // 3. 验证选题关系是否存在
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, createDTO.getStudentId())
                .eq(BizSelection::getTopicId, createDTO.getTopicId());
        BizSelection selection = bizSelectionMapper.selectOne(selectionWrapper);
        if (selection == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该学生未选择此课题");
        }

        // 4. 验证评分教师是否存在
        if (createDTO.getGraderId() != null) {
            BizTeacher teacher = bizTeacherMapper.selectById(createDTO.getGraderId());
            if (teacher == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "评分教师不存在");
            }
        }

        // 5. 验证成绩范围
        if (createDTO.getScore() != null) {
            if (createDTO.getScore().compareTo(BigDecimal.ZERO) < 0 || 
                createDTO.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "成绩必须在0-100之间");
            }
        }

        // 6. 检查是否已存在该学生的该课题成绩
        LambdaQueryWrapper<BizGrade> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(BizGrade::getStudentId, createDTO.getStudentId())
                .eq(BizGrade::getTopicId, createDTO.getTopicId());
        if (bizGradeMapper.selectCount(duplicateWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该学生在此课题上已有成绩记录");
        }

        // 7. 创建成绩实体
        BizGrade grade = new BizGrade();
        grade.setStudentId(createDTO.getStudentId());
        grade.setTopicId(createDTO.getTopicId());
        grade.setScore(createDTO.getScore());
        grade.setGraderId(createDTO.getGraderId());
        grade.setComment(createDTO.getComment());
        grade.setGradedAt(LocalDateTime.now());

        // 8. 插入数据库
        bizGradeMapper.insert(grade);
        
        // 9. 清除相关缓存
        clearStudentGradesCache(createDTO.getStudentId());
        clearTopicGradesCache(createDTO.getTopicId());
        if (createDTO.getGraderId() != null) {
            clearTeacherGradesCache(createDTO.getGraderId());
        }
    }

    /**
     * 更新成绩
     *
     * @param id 成绩ID
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional
    public void updateGrade(Long id, GradeUpdateDTO updateDTO) {
        // 1. 查询成绩是否存在
        BizGrade existingGrade = bizGradeMapper.selectById(id);
        if (existingGrade == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 验证新的学生是否存在
        if (updateDTO.getStudentId() != null) {
            BizStudent student = bizStudentMapper.selectById(updateDTO.getStudentId());
            if (student == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "学生不存在");
            }
        }

        // 3. 验证新的课题是否存在
        if (updateDTO.getTopicId() != null) {
            BizTopic topic = bizTopicMapper.selectById(updateDTO.getTopicId());
            if (topic == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "课题不存在");
            }
        }

        // 4. 验证新的评分教师是否存在
        if (updateDTO.getGraderId() != null) {
            BizTeacher teacher = bizTeacherMapper.selectById(updateDTO.getGraderId());
            if (teacher == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "评分教师不存在");
            }
        }

        // 5. 验证成绩范围
        if (updateDTO.getScore() != null) {
            if (updateDTO.getScore().compareTo(BigDecimal.ZERO) < 0 || 
                updateDTO.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "成绩必须在0-100之间");
            }
        }

        // 6. 构建更新实体
        BizGrade updateGrade = new BizGrade();
        updateGrade.setId(id);
        if (updateDTO.getStudentId() != null) {
            updateGrade.setStudentId(updateDTO.getStudentId());
        }
        if (updateDTO.getTopicId() != null) {
            updateGrade.setTopicId(updateDTO.getTopicId());
        }
        if (updateDTO.getScore() != null) {
            updateGrade.setScore(updateDTO.getScore());
        }
        if (updateDTO.getGraderId() != null) {
            updateGrade.setGraderId(updateDTO.getGraderId());
        }
        if (updateDTO.getComment() != null) {
            updateGrade.setComment(updateDTO.getComment());
        }
        updateGrade.setUpdatedAt(LocalDateTime.now());

        // 7. 执行更新
        bizGradeMapper.updateById(updateGrade);
        
        // 8. 清除缓存
        clearGradeCache(id);
        if (updateDTO.getStudentId() != null && !updateDTO.getStudentId().equals(existingGrade.getStudentId())) {
            clearStudentGradesCache(existingGrade.getStudentId());
            clearStudentGradesCache(updateDTO.getStudentId());
        }
        if (updateDTO.getTopicId() != null && !updateDTO.getTopicId().equals(existingGrade.getTopicId())) {
            clearTopicGradesCache(existingGrade.getTopicId());
            clearTopicGradesCache(updateDTO.getTopicId());
        }
        if (updateDTO.getGraderId() != null && !updateDTO.getGraderId().equals(existingGrade.getGraderId())) {
            if (existingGrade.getGraderId() != null) {
                clearTeacherGradesCache(existingGrade.getGraderId());
            }
            clearTeacherGradesCache(updateDTO.getGraderId());
        }
    }

    /**
     * 删除成绩
     *
     * @param id 成绩ID
     */
    @Override
    @Transactional
    public void deleteGrade(Long id) {
        // 1. 检查成绩是否存在
        BizGrade grade = bizGradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 执行删除（逻辑删除）
        bizGradeMapper.deleteById(id);
        
        // 3. 清除缓存
        clearGradeCache(id);
        clearStudentGradesCache(grade.getStudentId());
        clearTopicGradesCache(grade.getTopicId());
        if (grade.getGraderId() != null) {
            clearTeacherGradesCache(grade.getGraderId());
        }
    }

    /**
     * 将BizGrade实体转换为GradeVO
     *
     * @param grade 成绩实体
     * @return 成绩VO
     */
    private GradeVO convertToGradeVO(BizGrade grade) {
        GradeVO vo = new GradeVO();
        vo.setId(grade.getId());
        vo.setStudentId(grade.getStudentId());
        vo.setTopicId(grade.getTopicId());
        vo.setScore(grade.getScore());
        vo.setGraderId(grade.getGraderId());
        vo.setComment(grade.getComment());
        vo.setGradedAt(grade.getGradedAt());
        vo.setCreatedAt(grade.getCreatedAt());
        vo.setUpdatedAt(grade.getUpdatedAt());

        // 补充学生姓名信息
        if (grade.getStudentId() != null) {
            BizStudent student = bizStudentMapper.selectById(grade.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getStudentId()); // 这里应该关联用户表获取真实姓名
            }
        }

        // 补充课题标题信息
        if (grade.getTopicId() != null) {
            BizTopic topic = bizTopicMapper.selectById(grade.getTopicId());
            if (topic != null) {
                vo.setTopicTitle(topic.getTitle());
            }
        }

        return vo;
    }

    /**
     * 清除单个成绩缓存
     */
    private void clearGradeCache(Long gradeId) {
        if (gradeId != null) {
            String cacheKey = CacheConstants.KeyPrefix.GRADE_INFO + gradeId;
            redisTemplate.delete(cacheKey);
            log.debug("清除成绩缓存: " + cacheKey);
        }
    }

    /**
     * 清除学生相关成绩缓存
     */
    private void clearStudentGradesCache(Long studentId) {
        if (studentId != null) {
            // 可以扩展清除学生相关的成绩列表缓存
            log.debug("清除学生成绩相关缓存: " + studentId);
        }
    }

    /**
     * 清除课题相关成绩缓存
     */
    private void clearTopicGradesCache(Long topicId) {
        if (topicId != null) {
            // 可以扩展清除课题相关的成绩列表缓存
            log.debug("清除课题成绩相关缓存: " + topicId);
        }
    }

    /**
     * 清除教师相关成绩缓存
     */
    private void clearTeacherGradesCache(Long teacherId) {
        if (teacherId != null) {
            // 可以扩展清除教师相关的成绩列表缓存
            log.debug("清除教师成绩相关缓存: " + teacherId);
        }
    }
}