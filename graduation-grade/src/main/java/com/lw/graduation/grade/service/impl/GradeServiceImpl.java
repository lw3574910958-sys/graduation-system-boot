package com.lw.graduation.grade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeStatisticsQueryDTO;
import com.lw.graduation.api.service.grade.GradeService;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.GradeLevel;
import com.lw.graduation.grade.service.calculator.GradeCalculatorService;
import com.lw.graduation.grade.service.calculator.GradeDistribution;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 成绩服务实现类
 * 实现成绩管理模块的核心业务逻辑，包括成绩录入、自动计算、统计分析等功能。
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
    private final SysUserMapper sysUserMapper;
    private final CacheHelper cacheHelper;
    private final GradeCalculatorService gradeCalculatorService;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<GradeVO> getGradePage(GradePageQueryDTO queryDTO) {
        log.info("分页查询成绩列表: {}", queryDTO);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStudentId() != null, BizGrade::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizGrade::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getGraderId() != null, BizGrade::getGraderId, queryDTO.getGraderId())
                .ge(queryDTO.getMinScore() != null, BizGrade::getScore, queryDTO.getMinScore())
                .le(queryDTO.getMaxScore() != null, BizGrade::getScore, queryDTO.getMaxScore())
                .eq(BizGrade::getIsDeleted, 0)
                .orderByDesc(BizGrade::getGradedAt);

        // 2. 执行分页查询
        IPage<BizGrade> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizGrade> gradePage = bizGradeMapper.selectPage(page, wrapper);

        // 3. 转换为VO并批量填充关联信息（优化N+1查询）
        List<GradeVO> voList = convertToGradeVOListOptimized(gradePage.getRecords());
        IPage<GradeVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(voList);
        voPage.setTotal(gradePage.getTotal());

        return voPage;
    }

    @Override
    public GradeVO getGradeById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.GRADE_INFO + id;
        
        return cacheHelper.getFromCache(cacheKey, GradeVO.class, () -> {
            BizGrade grade = bizGradeMapper.selectById(id);
            if (grade == null || grade.getIsDeleted() == 1) {
                return null;
            }
            return convertToGradeVO(grade);
        }, CacheConstants.ExpireTime.WARM_DATA_EXPIRE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GradeVO inputGrade(GradeInputDTO inputDTO, Long graderId) {
        log.info("教师 {} 录入成绩: 学生={}, 题目={}, 成绩={}", 
                graderId, inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getScore());
        
        // 1. 验证录入权限
        validateGradeInputPermission(inputDTO.getStudentId(), inputDTO.getTopicId(), graderId);
        
        // 2. 检查是否已存在相同类型的成绩
        LambdaQueryWrapper<BizGrade> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizGrade::getStudentId, inputDTO.getStudentId())
                   .eq(BizGrade::getTopicId, inputDTO.getTopicId())
                   .eq(BizGrade::getGraderId, graderId)
                   .eq(BizGrade::getIsDeleted, 0);
        
        if (count(existWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该类型成绩已存在");
        }
        
        // 3. 如果是综合成绩，先尝试自动计算
        BigDecimal finalScore = inputDTO.getScore();
        if (inputDTO.getGradeType() == 3) { // 综合成绩
            finalScore = calculateCompositeGrade(inputDTO.getStudentId(), inputDTO.getTopicId());
        }
        
        // 4. 创建成绩记录
        BizGrade grade = new BizGrade();
        grade.setStudentId(inputDTO.getStudentId());
        grade.setTopicId(inputDTO.getTopicId());
        grade.setScore(finalScore);
        grade.setGraderId(graderId);
        grade.setComment(inputDTO.getComment());
        grade.setGradedAt(LocalDateTime.now());
        
        boolean saved = save(grade);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "成绩录入失败");
        }
        
        // 5. 清除相关缓存
        clearGradeCache(grade.getId());
        
        log.info("成绩录入成功，ID: {}", grade.getId());
        return convertToGradeVO(grade);
    }

    @Override
    public BigDecimal calculateCompositeGrade(Long studentId, Long topicId) {
        log.info("计算学生 {} 在题目 {} 的综合成绩", studentId, topicId);
        
        // 1. 获取该学生该题目的所有成绩
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getTopicId, topicId)
               .eq(BizGrade::getIsDeleted, 0);
        
        List<BizGrade> grades = list(wrapper);
        if (CollectionUtils.isEmpty(grades)) {
            return BigDecimal.ZERO;
        }
        
        // 2. 如果只有一个成绩，直接返回
        if (grades.size() == 1) {
            return grades.get(0).getScore();
        }
        
        // 3. 按照不同类型的成绩进行加权计算
        List<BigDecimal> scores = new ArrayList<>();
        List<BigDecimal> weights = new ArrayList<>();
        
        // 指导教师评分权重 0.4
        grades.stream()
                .filter(g -> g.getGraderId() != null && isAdvisorGrade(g.getGraderId(), topicId))
                .findFirst()
                .ifPresent(advisorGrade -> {
                    scores.add(advisorGrade.getScore());
                    weights.add(new BigDecimal("0.4"));
                });
        
        // 答辩评分权重 0.6
        grades.stream()
                .filter(g -> g.getGraderId() != null && !isAdvisorGrade(g.getGraderId(), topicId))
                .findFirst()
                .ifPresent(defenseGrade -> {
                    scores.add(defenseGrade.getScore());
                    weights.add(new BigDecimal("0.6"));
                });
        
        // 4. 计算加权平均成绩
        if (!scores.isEmpty() && scores.size() == weights.size()) {
            BigDecimal compositeScore = gradeCalculatorService.calculateWeightedAverage(scores, weights);
            log.info("综合成绩计算完成: {}", compositeScore);
            return compositeScore;
        }
        
        // 5. 如果无法按权重计算，则返回简单平均
        List<BigDecimal> allScores = grades.stream()
                .map(BizGrade::getScore)
                .collect(Collectors.toList());
        
        BigDecimal averageScore = gradeCalculatorService.calculateAverage(allScores);
        log.info("简单平均成绩: {}", averageScore);
        return averageScore;
    }

    @Override
    public List<GradeVO> getGradesByStudent(Long studentId) {
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getIsDeleted, 0)
               .orderByDesc(BizGrade::getGradedAt);
        
        return list(wrapper).stream()
                .map(this::convertToGradeVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeVO> getGradesByTeacher(Long teacherId) {
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getGraderId, teacherId)
               .eq(BizGrade::getIsDeleted, 0)
               .orderByDesc(BizGrade::getGradedAt);
        
        return list(wrapper).stream()
                .map(this::convertToGradeVO)
                .collect(Collectors.toList());
    }

    @Override
    public String getGradeStatistics(GradeStatisticsQueryDTO queryDTO) {
        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizGrade::getIsDeleted, 0);
            
            // 按教师筛选
            if (queryDTO.getTeacherId() != null) {
                wrapper.eq(BizGrade::getGraderId, queryDTO.getTeacherId());
            }
            
            // 按院系筛选（需要关联查询）
            if (queryDTO.getDepartmentId() != null) {
                // 这里需要复杂的关联查询，简化处理
                wrapper.apply("EXISTS (SELECT 1 FROM biz_topic t WHERE t.id = biz_grade.topic_id AND t.department_id = {0})", 
                             queryDTO.getDepartmentId());
            }
            
            // 只统计及格成绩
            if (Boolean.TRUE.equals(queryDTO.getPassingOnly())) {
                wrapper.ge(BizGrade::getScore, new BigDecimal("60"));
            }
            
            // 2. 查询成绩数据
            List<BizGrade> grades = list(wrapper);
            List<BigDecimal> scores = grades.stream()
                    .map(BizGrade::getScore)
                    .collect(Collectors.toList());
            
            // 3. 计算统计信息
            GradeDistribution distribution = gradeCalculatorService.calculateDistribution(scores);
            
            // 4. 转换为JSON字符串
            return objectMapper.writeValueAsString(distribution);
            
        } catch (Exception e) {
            log.error("成绩统计失败", e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "成绩统计失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGrade(Long id, Long graderId) {
        log.info("教师 {} 删除成绩: {}", graderId, id);
        
        // 1. 获取成绩信息
        BizGrade grade = getById(id);
        if (grade == null || grade.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "成绩不存在");
        }
        
        // 2. 验证删除权限
        if (!grade.getGraderId().equals(graderId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权删除他人录入的成绩");
        }
        
        // 3. 逻辑删除
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "成绩删除失败");
        }
        
        // 4. 清除缓存
        clearGradeCache(id);
        
        log.info("成绩删除成功，ID: {}", id);
        return true;
    }

    /**
     * 验证成绩录入权限
     */
    private void validateGradeInputPermission(Long studentId, Long topicId, Long graderId) {
        // 检查学生是否选择了该题目
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, studentId)
                       .eq(BizSelection::getTopicId, topicId)
                       .eq(BizSelection::getStatus, 1); // 已确认状态
        
        if (bizSelectionMapper.selectCount(selectionWrapper) == 0) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "该学生未选择此题目");
        }
        
        // 检查教师是否有权限评分（指导教师或答辩教师）
        BizTopic topic = bizTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }
        
        // 指导教师可以直接评分
        if (topic.getTeacherId().equals(graderId)) {
            return;
        }
        
        // 其他教师需要额外验证（如答辩小组成员等）
        // 这里简化处理，实际项目中可能需要更复杂的权限验证
    }

    /**
     * 判断是否为指导教师评分
     */
    private boolean isAdvisorGrade(Long graderId, Long topicId) {
        BizTopic topic = bizTopicMapper.selectById(topicId);
        return topic != null && topic.getTeacherId().equals(graderId);
    }

    /**
     * 转换成绩实体为VO
     */
    private GradeVO convertToGradeVO(BizGrade grade) {
        GradeVO vo = BeanMapperUtil.copyProperties(grade, GradeVO.class);
        
        // 填充扩展信息
        vo.setGradeLevel(grade.getGradeLevel());
        vo.setGpa(grade.getGPA());
        vo.setPassing(grade.isPass());
        vo.setExcellent(grade.isExcellent());
        
        // 填充学生信息
        if (grade.getStudentId() != null) {
            BizStudent student = bizStudentMapper.selectById(grade.getStudentId());
            if (student != null) {
                // 通过用户ID获取学生姓名
                SysUser studentUser = sysUserMapper.selectById(student.getUserId());
                if (studentUser != null) {
                    vo.setStudentName(studentUser.getRealName());
                }
                vo.setStudentNumber(student.getStudentId());
            }
        }
        
        // 填充题目信息
        if (grade.getTopicId() != null) {
            BizTopic topic = bizTopicMapper.selectById(grade.getTopicId());
            if (topic != null) {
                vo.setTopicTitle(topic.getTitle());
            }
        }
        
        // 填充评分教师信息
        if (grade.getGraderId() != null) {
            SysUser grader = sysUserMapper.selectById(grade.getGraderId());
            if (grader != null) {
                vo.setGraderName(grader.getRealName());
            }
        }
        
        return vo;
    }

    /**
     * 批量转换成绩实体为VO（优化N+1查询）
     * 通过批量查询减少数据库访问次数
     */
    private List<GradeVO> convertToGradeVOListOptimized(List<BizGrade> grades) {
        if (grades == null || grades.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取所有需要查询的ID
        List<Long> gradeIds = grades.stream()
                .map(BizGrade::getId)
                .collect(Collectors.toList());
        
        // 批量查询关联信息
        List<Map<String, Object>> gradeDetails = bizGradeMapper.selectGradeDetailsWithRelations(gradeIds);
        
        // 构建ID到详情的映射
        Map<Long, Map<String, Object>> detailsMap = gradeDetails.stream()
                .collect(Collectors.toMap(
                        detail -> ((Number) detail.get("id")).longValue(),
                        detail -> detail,
                        (existing, replacement) -> existing
                ));
        
        // 转换为VO列表
        return grades.stream().map(grade -> {
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
            
            // 填充扩展信息
            vo.setGradeLevel(grade.getGradeLevel());
            vo.setGpa(grade.getGPA());
            vo.setPassing(grade.isPass());
            vo.setExcellent(grade.isExcellent());
            
            // 从批量查询结果中获取关联信息
            Map<String, Object> detail = detailsMap.get(grade.getId());
            if (detail != null) {
                vo.setStudentNumber((String) detail.get("student_number"));
                vo.setStudentName((String) detail.get("student_name"));
                vo.setTopicTitle((String) detail.get("topic_title"));
                vo.setGraderName((String) detail.get("grader_name"));
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 清除成绩相关缓存
     */
    private void clearGradeCache(Long gradeId) {
        String cacheKey = CacheConstants.KeyPrefix.GRADE_INFO + gradeId;
        cacheHelper.evictCache(cacheKey);
    }
}