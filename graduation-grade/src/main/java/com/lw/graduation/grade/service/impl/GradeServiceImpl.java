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
import com.lw.graduation.api.vo.grade.GradeExportVO;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.auth.service.PermissionValidationService;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.grade.GradeType;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.grade.service.calculator.GradeCalculatorService;
import com.lw.graduation.grade.service.calculator.GradeDistribution;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final SysUserMapper sysUserMapper;
    private final BizTeacherMapper bizTeacherMapper;
    private final CacheHelper cacheHelper;
    private final GradeCalculatorService gradeCalculatorService;
    private final ObjectMapper objectMapper;
    private final PermissionValidationService permissionValidationService;
    private final DataPermissionUtil dataPermissionUtil;

    @Override
    public IPage<GradeVO> getGradePage(GradePageQueryDTO queryDTO) {
        log.info("分页查询成绩列表 - 当前页: {}, 每页大小: {}, 学生ID: {}, 学生姓名: {}, 学生学号: {}, 题目ID: {}, 教师ID: {}, 教师姓名: {}, 教师工号: {}, 分数范围: {}-{}, 成绩等级: {}, 绩点: {}", 
                queryDTO.getCurrent(), queryDTO.getSize(), 
                queryDTO.getStudentId(), queryDTO.getStudentName(), queryDTO.getStudentNumber(),
                queryDTO.getTopicId(), queryDTO.getGraderId(), queryDTO.getGraderName(), queryDTO.getGraderWorkNumber(),
                queryDTO.getMinScore(), queryDTO.getMaxScore(),
                queryDTO.getGradeLevel(), queryDTO.getGpa());
        
        // 1. 构建查询条件
        LambdaQueryWrapper<BizGrade> wrapper = buildGradeQueryWrapper(queryDTO);
        if (wrapper == null) {
            // 子查询无结果，返回空页面
            return createEmptyPage(queryDTO.getCurrent(), queryDTO.getSize());
        }

        // 2. 执行分页查询
        IPage<BizGrade> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizGrade> gradePage = bizGradeMapper.selectPage(page, wrapper);

        // 3. 转换为VO并批量填充关联信息（优化N+1查询）
        List<GradeVO> voList = convertToGradeVOListOptimized(gradePage.getRecords());
        
        // 4. 过滤计算字段（成绩等级和绩点）
        voList = filterByCalculatedFields(voList, queryDTO.getGradeLevel(), queryDTO.getGpa());
        
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
            if (grade == null || grade.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
                return null;
            }
            return convertToGradeVO(grade);
        }, CacheConstants.ExpireTime.WARM_DATA_EXPIRE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GradeVO inputGrade(GradeInputDTO inputDTO, Long graderId) {
        log.info("教师 {} 录入成绩（首次录入）：学生={}, 题目={}, 类型={}, 成绩={}", 
                graderId, inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getGradeType(), inputDTO.getScore());
        
        // 1. 验证录入权限
        permissionValidationService.validateGradeInputPermission(inputDTO.getStudentId(), inputDTO.getTopicId(), graderId);
        
        // 2. 检查是否已存在相同类型的成绩
        LambdaQueryWrapper<BizGrade> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizGrade::getStudentId, inputDTO.getStudentId())
               .eq(BizGrade::getTopicId, inputDTO.getTopicId())
               .eq(BizGrade::getGradeType, inputDTO.getGradeType())
               .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        
        BizGrade existingGrade = getOne(existWrapper);
        
        if (existingGrade != null) {
            // 成绩记录已存在
            if (existingGrade.getScore() != null) {
                // 已有分数，说明已经录入过，不允许修改
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该类型成绩已录入，不允许修改");
            }
            
            // 验证教师权限：只能录入自己的成绩记录
            if (!existingGrade.getGraderId().equals(graderId)) {
                throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权录入该成绩记录");
            }
            
            // 3. 执行首次录入（仅允许录入 score 和 comment）
            if (inputDTO.getScore() == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "成绩分数不能为空");
            }
            
            // 验证成绩范围
            if (inputDTO.getScore().compareTo(BigDecimal.ZERO) < 0 || 
                inputDTO.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "成绩必须在 0-100 之间");
            }
            
            existingGrade.setScore(inputDTO.getScore());
            existingGrade.setComment(inputDTO.getComment());
            
            // 设置评分时间（首次录入时）
            if (existingGrade.getGradedAt() == null) {
                existingGrade.setGradedAt(LocalDateTime.now());
            }
            
            log.info("成绩录入 - ID: {}, 学生：{}, 类型：{}, 分数：{}, 评分时间：{}", 
                    existingGrade.getId(), existingGrade.getStudentId(), 
                    existingGrade.getGradeType(), inputDTO.getScore(), existingGrade.getGradedAt());
            
            boolean updated = updateById(existingGrade);
            if (!updated) {
                throw new BusinessException(ResponseCode.ERROR.getCode(), "成绩录入失败");
            }
            
            clearGradeCache(existingGrade.getId());
            
            // 如果是毕业论文评分且已完成（有分数），自动计算并保存综合成绩
            if (existingGrade.getGradeType() != null && existingGrade.getGradeType().equals(GradeType.THESIS_GRADE.getCode()) && existingGrade.getScore() != null) { // 毕业论文教师评分
                tryAutoSaveCompositeGrade(existingGrade.getStudentId(), existingGrade.getTopicId(), existingGrade.getGraderId());
            }
            
            log.info("成绩录入成功，ID: {}", existingGrade.getId());
            return convertToGradeVO(existingGrade);
        } else {
            // 成绩记录不存在，执行创建操作（自动创建场景）
            return inputGradeInternal(inputDTO, graderId, true, false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inputGradeForAutoCreate(GradeInputDTO inputDTO, Long graderId) {
        log.info("自动创建成绩记录：学生={}, 题目={}, 类型={}", 
                inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getGradeType());
        
        // 自动创建时不设置评分时间，由教师正式评分时设置
        inputGradeInternal(inputDTO, graderId, false, false);
    }

    /**
     * 内部成绩录入方法
     * 
     * @param inputDTO 成绩录入 DTO
     * @param graderId 评分教师 ID
     * @param setGradedAt 是否设置评分时间
     * @param allowComposite 是否允许创建综合成绩（仅系统自动调用时使用）
     * @return 成绩 VO
     */
    private GradeVO inputGradeInternal(GradeInputDTO inputDTO, Long graderId, boolean setGradedAt, boolean allowComposite) {
        log.info("教师 {} 录入成绩：学生={}, 题目={}, 类型={}, 成绩={}", 
                graderId, inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getGradeType(), inputDTO.getScore());
        
        // 1. 禁止手动录入综合成绩（综合成绩由系统自动计算生成）
        if (!allowComposite && inputDTO.getGradeType() != null && inputDTO.getGradeType().equals(GradeType.COMPOSITE_GRADE.getCode())) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "综合成绩由系统自动计算生成，不允许手动录入");
        }
        
        // 2. 验证录入权限
        permissionValidationService.validateGradeInputPermission(inputDTO.getStudentId(), inputDTO.getTopicId(), graderId);
        
        // 3. 检查是否已存在相同类型的成绩（仅针对正式评分场景）
        if (inputDTO.getScore() != null) {
            LambdaQueryWrapper<BizGrade> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(BizGrade::getStudentId, inputDTO.getStudentId())
                       .eq(BizGrade::getTopicId, inputDTO.getTopicId())
                       .eq(BizGrade::getGradeType, inputDTO.getGradeType())
                       .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            if (count(existWrapper) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该类型成绩已存在，请勿重复录入");
            }
        }
        
        // 4. 准备分数值（直接使用 DTO 中的分数）
        BigDecimal finalScore = inputDTO.getScore();
        
        // 使用计算器服务验证成绩（仅在有分数时）
        String gradeLevel = null;
        if (finalScore != null) {
            boolean isPassing = gradeCalculatorService.isPassing(finalScore);
            gradeLevel = gradeCalculatorService.getGradeLevel(finalScore);
            
            log.info("成绩验证 - 学生：{}, 题目：{}, 类型：{}, 最终分数：{}, 及格：{}, 等级：{}", 
                    inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getGradeType(), finalScore, isPassing, gradeLevel);
        }
        
        // 4. 创建成绩记录
        BizGrade grade = new BizGrade();
        grade.setStudentId(inputDTO.getStudentId());
        grade.setTopicId(inputDTO.getTopicId());
        grade.setGradeType(inputDTO.getGradeType());
        grade.setScore(finalScore); // 可以为 null（待录入状态）
        grade.setGraderId(graderId);
        grade.setComment(inputDTO.getComment()); // 可以为 null（待录入状态）
        
        // 只有在有分数时才设置评分时间（教师正式评分）
        if (setGradedAt && finalScore != null) {
            grade.setGradedAt(LocalDateTime.now());
        }
        
        log.info("成绩保存 - 学生 ID: {}, 类型：{}, 分数：{}, 等级：{}, 评分教师：{}, 设置评分时间：{}", 
                inputDTO.getStudentId(), inputDTO.getGradeType(), finalScore, gradeLevel, graderId, setGradedAt && finalScore != null);
        
        boolean saved = save(grade);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "成绩录入失败");
        }
        
        // 5. 清除相关缓存
        clearGradeCache(grade.getId());
        
        // 5. 如果是毕业论文评分且已完成（有分数），自动计算并保存综合成绩
        if (inputDTO.getGradeType() != null && inputDTO.getGradeType().equals(GradeType.THESIS_GRADE.getCode()) && finalScore != null) { // 毕业论文教师评分
            tryAutoSaveCompositeGrade(inputDTO.getStudentId(), inputDTO.getTopicId(), graderId);
        }
        
        log.info("成绩录入成功，ID: {}", grade.getId());
        return convertToGradeVO(grade);
    }

    @Override
    public void tryAutoSaveCompositeGrade(Long studentId, Long topicId, Long graderId) {
        log.info("检查是否可以自动保存综合成绩：studentId={}, topicId={}", studentId, topicId);
        
        // 1. 查询该学生该题目的所有成绩
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getTopicId, topicId)
               .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        
        List<BizGrade> grades = list(wrapper);
        
        // 2. 检查是否三种类型成绩都存在且有分数
        boolean hasProposal = grades.stream()
                .anyMatch(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.PROPOSAL_GRADE.getCode()) && g.getScore() != null);
        boolean hasMidterm = grades.stream()
                .anyMatch(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.MIDTERM_GRADE.getCode()) && g.getScore() != null);
        boolean hasThesis = grades.stream()
                .anyMatch(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.THESIS_GRADE.getCode()) && g.getScore() != null);
        
        // 3. 如果三种成绩都存在，计算并保存综合成绩
        if (hasProposal && hasMidterm && hasThesis) {
            log.info("三种成绩类型已完成，开始计算综合成绩：studentId={}, topicId={}", studentId, topicId);
            
            // 检查是否已存在综合成绩
            LambdaQueryWrapper<BizGrade> compositeWrapper = new LambdaQueryWrapper<>();
            compositeWrapper.eq(BizGrade::getStudentId, studentId)
                           .eq(BizGrade::getTopicId, topicId)
                           .eq(BizGrade::getGradeType, GradeType.COMPOSITE_GRADE.getCode()) // 综合成绩
                           .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            long count = count(compositeWrapper);
            
            if (count > 0) {
                log.info("综合成绩已存在，跳过自动保存：studentId={}, topicId={}", studentId, topicId);
                return;
            }
            
            // 计算综合成绩
            BigDecimal compositeScore = calculateCompositeGrade(studentId, topicId);
            
            if (compositeScore != null && compositeScore.compareTo(BigDecimal.ZERO) > 0) {
                // 计算等级和是否及格
                Boolean isPassing = gradeCalculatorService.isPassing(compositeScore);
                String gradeLevel = gradeCalculatorService.getGradeLevel(compositeScore);
                
                // 组合评语（开题 + 中期 + 论文）
                String compositeComment = buildCompositeComment(studentId, topicId);
                
                // 创建综合成绩记录
                GradeInputDTO compositeDTO = new GradeInputDTO();
                compositeDTO.setStudentId(studentId);
                compositeDTO.setTopicId(topicId);
                compositeDTO.setGradeType(GradeType.COMPOSITE_GRADE.getCode()); // 综合成绩
                compositeDTO.setScore(compositeScore);
                compositeDTO.setComment(compositeComment); // 设置组合后的评语
                
                log.info("自动保存综合成绩：studentId={}, topicId={}, score={}, gradeLevel={}, passing={}, commentLength={}", 
                        studentId, topicId, compositeScore, gradeLevel, isPassing, 
                        compositeComment != null ? compositeComment.length() : 0);
                
                // 使用内部方法保存（允许创建综合成绩）
                inputGradeInternal(compositeDTO, graderId, true, true);
                
                log.info("综合成绩自动保存成功：studentId={}, topicId={}, gradeId={}", 
                        studentId, topicId, compositeDTO.getStudentId());
            }
        } else {
            log.debug("三种成绩类型未全部完成，暂不计算综合成绩：开题={}, 中期={}, 论文={}", 
                    hasProposal, hasMidterm, hasThesis);
        }
    }

    /**
     * 组合综合成绩评语（开题 + 中期 + 论文）
     * 
     * @param studentId 学生 ID
     * @param topicId 题目 ID
     * @return 组合后的评语
     */
    private String buildCompositeComment(Long studentId, Long topicId) {
        log.debug("构建综合成绩评语：studentId={}, topicId={}", studentId, topicId);
        
        // 1. 查询该学生该题目的所有成绩
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getTopicId, topicId)
               .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode())
               .orderByAsc(BizGrade::getGradeType); // 按成绩类型排序
        
        List<BizGrade> grades = list(wrapper);
        if (grades.isEmpty()) {
            return null;
        }
        
        // 2. 组合各项评语
        StringBuilder compositeComment = new StringBuilder();
        
        // 开题报告评语
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.PROPOSAL_GRADE.getCode()))
                .findFirst()
                .ifPresent(proposalGrade -> {
                    compositeComment.append("【开题报告】");
                    if (proposalGrade.getComment() != null && !proposalGrade.getComment().isEmpty()) {
                        compositeComment.append(proposalGrade.getComment());
                    } else {
                        compositeComment.append("无评语");
                    }
                    compositeComment.append("\n\n");
                });
        
        // 中期报告评语
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.MIDTERM_GRADE.getCode()))
                .findFirst()
                .ifPresent(midtermGrade -> {
                    compositeComment.append("【中期报告】");
                    if (midtermGrade.getComment() != null && !midtermGrade.getComment().isEmpty()) {
                        compositeComment.append(midtermGrade.getComment());
                    } else {
                        compositeComment.append("无评语");
                    }
                    compositeComment.append("\n\n");
                });
        
        // 毕业论文评语
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.THESIS_GRADE.getCode()))
                .findFirst()
                .ifPresent(thesisGrade -> {
                    compositeComment.append("【毕业论文】");
                    if (thesisGrade.getComment() != null && !thesisGrade.getComment().isEmpty()) {
                        compositeComment.append(thesisGrade.getComment());
                    } else {
                        compositeComment.append("无评语");
                    }
                });
        
        String result = compositeComment.toString().trim();
        log.debug("综合评语构建完成，长度：{}", result.length());
        
        return result.isEmpty() ? null : result;
    }

    @Override
    public BigDecimal calculateCompositeGrade(Long studentId, Long topicId) {
        log.info("计算学生 {} 在题目 {} 的综合成绩", studentId, topicId);
        
        // 1. 获取该学生该题目的所有成绩
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getTopicId, topicId)
               .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        
        List<BizGrade> grades = list(wrapper);
        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 2. 如果只有一个成绩，直接返回
        if (grades.size() == 1) {
            return grades.getFirst().getScore();
        }
        
        // 3. 按照不同类型的成绩进行加权计算
        // 开题报告权重 0.3，中期报告权重 0.3，毕业论文权重 0.4
        List<BigDecimal> scores = new ArrayList<>();
        List<BigDecimal> weights = new ArrayList<>();
        
        // 开题报告评分权重 0.3
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.PROPOSAL_GRADE.getCode())) // 开题报告教师评分
                .findFirst()
                .ifPresent(proposalGrade -> {
                    scores.add(proposalGrade.getScore());
                    weights.add(new BigDecimal("0.3"));
                });
        
        // 中期报告评分权重 0.3
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.MIDTERM_GRADE.getCode())) // 中期报告教师评分
                .findFirst()
                .ifPresent(midtermGrade -> {
                    scores.add(midtermGrade.getScore());
                    weights.add(new BigDecimal("0.3"));
                });
        
        // 毕业论文评分权重 0.4
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType().equals(GradeType.THESIS_GRADE.getCode())) // 毕业论文教师评分
                .findFirst()
                .ifPresent(thesisGrade -> {
                    scores.add(thesisGrade.getScore());
                    weights.add(new BigDecimal("0.4"));
                });
        
        // 4. 计算加权平均成绩
        if (!scores.isEmpty() && scores.size() == weights.size()) {
            BigDecimal compositeScore = gradeCalculatorService.calculateWeightedAverage(scores, weights);
            
            // 同时计算总成绩和平均绩点
            BigDecimal totalScore = gradeCalculatorService.calculateTotal(scores);
            BigDecimal averageGPA = gradeCalculatorService.calculateAverageGPA(scores);
            
            log.info("综合成绩计算完成 - 加权平均: {}, 总成绩: {}, 平均绩点: {}", 
                    compositeScore, totalScore, averageGPA);
            return compositeScore;
        }
        
        // 5. 如果无法按权重计算，则返回简单平均
        List<BigDecimal> allScores = grades.stream()
                .map(BizGrade::getScore)
                .toList();
        
        BigDecimal averageScore = gradeCalculatorService.calculateAverage(allScores);
        
        // 计算额外的统计指标
        BigDecimal totalScore = gradeCalculatorService.calculateTotal(allScores);
        BigDecimal averageGPA = gradeCalculatorService.calculateAverageGPA(allScores);
        
        log.info("简单平均成绩计算 - 平均分: {}, 总分: {}, 平均绩点: {}", 
                averageScore, totalScore, averageGPA);
        return averageScore;
    }

    @Override
    public List<GradeVO> getGradesByStudent(Long studentId) {
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode())
               .orderByDesc(BizGrade::getGradedAt);
        
        return list(wrapper).stream()
                .map(this::convertToGradeVO)
                .toList();
    }

    @Override
    public List<GradeVO> getGradesByTeacher(Long teacherId) {
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getGraderId, teacherId)
               .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode())
               .orderByDesc(BizGrade::getGradedAt);
        
        return list(wrapper).stream()
                .map(this::convertToGradeVO)
                .toList();
    }

    @Override
    public String getGradeStatistics(GradeStatisticsQueryDTO queryDTO) {
        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
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
                    .toList();
            
            // 3. 计算统计信息
            GradeDistribution distribution = gradeCalculatorService.calculateDistribution(scores);
            
            // 4. 计算额外统计指标
            BigDecimal totalScore = gradeCalculatorService.calculateTotal(scores);
            BigDecimal averageGPA = gradeCalculatorService.calculateAverageGPA(scores);
            
            // 5. 添加各等级比例信息和计算统计指标
            BigDecimal excellentPercentage = distribution.getLevelPercentage("excellent");
            BigDecimal goodPercentage = distribution.getLevelPercentage("good");
            BigDecimal fairPercentage = distribution.getLevelPercentage("fair");
            BigDecimal passPercentage = distribution.getLevelPercentage("pass");
            BigDecimal failPercentage = distribution.getLevelPercentage("fail");
                    
            // 计算排名百分比
            BigDecimal averageScore = gradeCalculatorService.calculateAverage(scores);
            BigDecimal percentileRank = gradeCalculatorService.calculatePercentileRank(averageScore, scores);
                    
            log.info("成绩分布统计 - 总数: {}, 总分: {}, 平均绩点: {}, 及格率: {}%, 平均分排名: {}%", 
                    distribution.getTotalCount(), totalScore, averageGPA, distribution.getPassRate(), percentileRank);
                    
            log.info("等级分布详情 - 优秀: {}%({}), 良好: {}%({}), 中等: {}%({}), 及格: {}%({}), 不及格: {}%({})",
                    excellentPercentage, distribution.getExcellentCount(),
                    goodPercentage, distribution.getGoodCount(),
                    fairPercentage, distribution.getFairCount(),
                    passPercentage, distribution.getPassCount(),
                    failPercentage, distribution.getFailCount());
            
            // 计算排名百分比（以最高分为例进行分析）
            if (!scores.isEmpty()) {
                BigDecimal highestInBatch = scores.stream()
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                BigDecimal batchPercentileRank = gradeCalculatorService.calculatePercentileRank(highestInBatch, scores);
                log.info("排名分析 - 批次最高分: {}, 超越百分比: {}%", highestInBatch, batchPercentileRank);
            }
            
            // 5. 转换为 JSON 字符串
            return objectMapper.writeValueAsString(distribution);
                
        } catch (Exception e) {
            log.error("成绩统计失败", e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "成绩统计失败");
        }
    }

    @Override
    public List<GradeExportVO> exportGrades(GradePageQueryDTO queryDTO) {
        log.info("导出成绩列表 - 学生ID: {}, 学生姓名: {}, 学生学号: {}, 题目ID: {}, 教师ID: {}, 教师姓名: {}, 教师工号: {}, 分数范围: {}-{}, 成绩等级: {}, 绩点: {}", 
                queryDTO.getStudentId(), queryDTO.getStudentName(), queryDTO.getStudentNumber(),
                queryDTO.getTopicId(), queryDTO.getGraderId(), queryDTO.getGraderName(), queryDTO.getGraderWorkNumber(),
                queryDTO.getMinScore(), queryDTO.getMaxScore(),
                queryDTO.getGradeLevel(), queryDTO.getGpa());
        
        // 1. 构建查询条件（与分页查询保持一致，但不分页）
        LambdaQueryWrapper<BizGrade> wrapper = buildGradeQueryWrapper(queryDTO);
        if (wrapper == null) {
            return new ArrayList<>(); // 子查询无结果
        }

        // 2. 查询所有符合条件的数据
        List<BizGrade> grades = list(wrapper);
        
        // 3. 批量填充关联信息并转换为 VO
        List<GradeVO> voList = convertToGradeVOListOptimized(grades);
        
        // 4. 过滤计算字段（成绩等级和绩点）
        voList = filterByCalculatedFields(voList, queryDTO.getGradeLevel(), queryDTO.getGpa());
        
        // 5. 转换为导出 VO
        return voList.stream().map(vo -> {
            GradeExportVO exportVO = new GradeExportVO();
            exportVO.setStudentName(vo.getStudentName());
            exportVO.setStudentNumber(vo.getStudentNumber());
            exportVO.setTopicTitle(vo.getTopicTitle());
            exportVO.setGradeTypeDesc(vo.getGradeTypeDesc());
            exportVO.setScore(vo.getScore() != null ? vo.getScore().doubleValue() : null);
            exportVO.setGradeLevel(vo.getGradeLevel());
            exportVO.setGpa(vo.getGpa() != null ? vo.getGpa().doubleValue() : null);
            
            // 组装评分教师显示：姓名 - 工号
            if (vo.getGraderName() != null && vo.getGraderWorkNumber() != null) {
                exportVO.setGraderDisplay(vo.getGraderName() + " - " + vo.getGraderWorkNumber());
            } else if (vo.getGraderName() != null) {
                exportVO.setGraderDisplay(vo.getGraderName());
            } else {
                exportVO.setGraderDisplay(vo.getGraderWorkNumber());
            }
            
            exportVO.setGradedAt(vo.getGradedAt() != null ? vo.getGradedAt().toString() : null);
            exportVO.setComment(vo.getComment());
            return exportVO;
        }).collect(Collectors.toList());
    }
    
    /**
     * 转换成绩实体为 VO
     */
    private GradeVO convertToGradeVO(BizGrade grade) {
        GradeVO vo = BeanMapperUtil.copyProperties(grade, GradeVO.class);
            
        // 填充成绩类型描述
        if (grade.getGradeType() != null) {
            GradeType gradeType = 
                com.lw.graduation.common.enums.IEnum.getByCode(
                    GradeType.class, 
                    grade.getGradeType()
                );
            if (gradeType != null) {
                vo.setGradeTypeDesc(gradeType.getDescription());
            }
        }
            
        // 填充扩展信息
        vo.setGradeLevel(grade.getGradeLevel());
        vo.setGpa(grade.getGPA());
        vo.setPassing(grade.isPass());
        vo.setExcellent(grade.isExcellent());
            
        // 填充学生信息
        if (grade.getStudentId() != null) {
            BizStudent student = bizStudentMapper.selectById(grade.getStudentId());
            if (student != null) {
                // 通过用户 ID 获取学生姓名
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
                // 通过 teacher 表获取工号
                LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
                teacherWrapper.eq(BizTeacher::getUserId, grade.getGraderId());
                BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
                if (teacher != null) {
                    vo.setGraderWorkNumber(teacher.getTeacherId());
                }
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
                .toList();
        
        // 批量查询关联信息
        List<Map<String, Object>> gradeDetails = bizGradeMapper.selectDetailsWithRelations(gradeIds);
        
        // 构建ID到详情的映射
        Map<Long, Map<String, Object>> detailsMap = gradeDetails.stream()
                .collect(Collectors.toMap(
                        detail -> ((Number) detail.get("id")).longValue(),
                        detail -> detail,
                        (existing, replacement) -> existing
                ));
        
        // 转换为 VO 列表
        return grades.stream().map(grade -> {
            GradeVO vo = new GradeVO();
            vo.setId(grade.getId());
            vo.setStudentId(grade.getStudentId());
            vo.setTopicId(grade.getTopicId());
            vo.setGradeType(grade.getGradeType());
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
                    
            // 填充成绩类型描述
            GradeType gradeType = 
                com.lw.graduation.common.enums.IEnum.getByCode(
                    GradeType.class,
                    grade.getGradeType()
                );
            if (gradeType != null) {
                vo.setGradeTypeDesc(gradeType.getDescription());
            }
                    
            // 从批量查询结果中获取关联信息
            Map<String, Object> detail = detailsMap.get(grade.getId());
            if (detail != null) {
                vo.setStudentNumber((String) detail.get("student_number"));
                vo.setStudentName((String) detail.get("student_name"));
                vo.setTopicTitle((String) detail.get("topic_title"));
                vo.setGraderName((String) detail.get("grader_name"));
                vo.setGraderWorkNumber((String) detail.get("grader_work_number"));
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

    /**
     * 构建成绩查询条件包装器
     * 
     * @param queryDTO 查询参数
     * @return 查询条件包装器，如果子查询无结果则返回 null
     */
    private LambdaQueryWrapper<BizGrade> buildGradeQueryWrapper(GradePageQueryDTO queryDTO) {
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        
        // 基本字段查询
        wrapper.eq(queryDTO.getStudentId() != null, BizGrade::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizGrade::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getGraderId() != null, BizGrade::getGraderId, queryDTO.getGraderId())
                .eq(queryDTO.getGradeType() != null, BizGrade::getGradeType, queryDTO.getGradeType())
                .ge(queryDTO.getMinScore() != null, BizGrade::getScore, queryDTO.getMinScore())
                .le(queryDTO.getMaxScore() != null, BizGrade::getScore, queryDTO.getMaxScore())
                .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        
        // 学生姓名模糊查询（通过子查询）
        if (queryDTO.getStudentName() != null && !queryDTO.getStudentName().trim().isEmpty()) {
            List<Long> studentIds = dataPermissionUtil.findStudentIdsByName(queryDTO.getStudentName());
            if (studentIds.isEmpty()) {
                return null; // 无匹配结果
            }
            wrapper.in(BizGrade::getStudentId, studentIds);
        }
        
        // 学生学号模糊查询（通过子查询）
        if (queryDTO.getStudentNumber() != null && !queryDTO.getStudentNumber().trim().isEmpty()) {
            List<Long> studentIds = dataPermissionUtil.findStudentIdsByNumber(queryDTO.getStudentNumber());
            if (studentIds.isEmpty()) {
                return null; // 无匹配结果
            }
            wrapper.in(BizGrade::getStudentId, studentIds);
        }
        
        // 教师姓名模糊查询（通过子查询）
        if (queryDTO.getGraderName() != null && !queryDTO.getGraderName().trim().isEmpty()) {
            List<Long> graderIds = dataPermissionUtil.findTeacherUserIdsByName(queryDTO.getGraderName());
            if (graderIds.isEmpty()) {
                return null; // 无匹配结果
            }
            wrapper.in(BizGrade::getGraderId, graderIds);
        }
        
        // 教师工号模糊查询（通过子查询）
        if (queryDTO.getGraderWorkNumber() != null && !queryDTO.getGraderWorkNumber().trim().isEmpty()) {
            List<Long> graderIds = dataPermissionUtil.findTeacherUserIdsByWorkNumber(queryDTO.getGraderWorkNumber());
            if (graderIds.isEmpty()) {
                return null; // 无匹配结果
            }
            wrapper.in(BizGrade::getGraderId, graderIds);
        }
        
        // 排序
        wrapper.orderByDesc(BizGrade::getGradedAt);
        
        return wrapper;
    }

    /**
     * 创建空分页结果
     * 
     * @param current 当前页
     * @param size 每页大小
     * @return 空分页对象
     */
    private <T> IPage<T> createEmptyPage(Integer current, Integer size) {
        IPage<T> emptyPage = new Page<>(current, size);
        emptyPage.setRecords(new ArrayList<>());
        emptyPage.setTotal(0);
        return emptyPage;
    }

    /**
     * 根据计算字段过滤列表（成绩等级和绩点）
     * 
     * @param voList VO列表
     * @param gradeLevel 成绩等级
     * @param gpa 绩点
     * @return 过滤后的列表
     */
    private List<GradeVO> filterByCalculatedFields(List<GradeVO> voList, String gradeLevel, BigDecimal gpa) {
        if (gradeLevel == null && gpa == null) {
            return voList; // 无需过滤
        }
        
        return voList.stream()
                .filter(vo -> {
                    // 过滤成绩等级
                    if (gradeLevel != null && !gradeLevel.equals(vo.getGradeLevel())) {
                        return false;
                    }
                    // 过滤绩点
                    if (gpa != null) {
                        if (vo.getGpa() == null) {
                            return false;
                        }
                        // 使用 compareTo 进行精确比较
                        return gpa.compareTo(vo.getGpa()) == 0;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 导出成绩报表响应（包含完整的HTTP响应头设置）
     *
     * @param queryDTO 查询条件
     * @return ResponseEntity<byte[]>
     */
    @Override
    public ResponseEntity<byte[]> exportGradesResponse(GradePageQueryDTO queryDTO) {
        try {
            List<GradeExportVO> dataList = exportGrades(queryDTO);
            
            // 使用 EasyExcel 写入到字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            com.alibaba.excel.EasyExcel.write(outputStream, GradeExportVO.class)
                    .sheet("成绩列表")
                    .doWrite(dataList);
            
            byte[] bytes = outputStream.toByteArray();
            
            // 设置响应头
            String fileName = URLEncoder.encode("成绩报表_" + System.currentTimeMillis(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", null);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName + ".xlsx");
            headers.setContentLength(bytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            log.error("成绩导出失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}