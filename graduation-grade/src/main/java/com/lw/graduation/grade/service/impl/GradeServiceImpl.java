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
import com.lw.graduation.grade.service.calculator.GradeCalculatorService;
import com.lw.graduation.grade.service.calculator.GradeDistribution;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    public IPage<GradeVO> getGradePage(GradePageQueryDTO queryDTO) {
        log.info("分页查询成绩列表 - 当前页: {}, 每页大小: {}, 学生ID: {}, 题目ID: {}, 教师ID: {}, 分数范围: {}-{}", 
                queryDTO.getCurrent(), queryDTO.getSize(), 
                queryDTO.getStudentId(), queryDTO.getTopicId(), queryDTO.getGraderId(),
                queryDTO.getMinScore(), queryDTO.getMaxScore());
        
        // 1. 构建查询条件
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStudentId() != null, BizGrade::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizGrade::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getGraderId() != null, BizGrade::getGraderId, queryDTO.getGraderId())
                .eq(queryDTO.getGradeType() != null, BizGrade::getGradeType, queryDTO.getGradeType())
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
        log.info("教师 {} 录入成绩（首次录入）：学生={}, 题目={}, 类型={}, 成绩={}", 
                graderId, inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getGradeType(), inputDTO.getScore());
        
        // 1. 验证录入权限
        permissionValidationService.validateGradeInputPermission(inputDTO.getStudentId(), inputDTO.getTopicId(), graderId);
        
        // 2. 检查是否已存在相同类型的成绩
        LambdaQueryWrapper<BizGrade> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizGrade::getStudentId, inputDTO.getStudentId())
               .eq(BizGrade::getTopicId, inputDTO.getTopicId())
               .eq(BizGrade::getGradeType, inputDTO.getGradeType())
               .eq(BizGrade::getIsDeleted, 0);
        
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
            if (inputDTO.getScore().compareTo(java.math.BigDecimal.ZERO) < 0 || 
                inputDTO.getScore().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "成绩必须在 0-100 之间");
            }
            
            existingGrade.setScore(inputDTO.getScore());
            existingGrade.setComment(inputDTO.getComment());
            
            // 设置评分时间（首次录入时）
            if (existingGrade.getGradedAt() == null) {
                existingGrade.setGradedAt(java.time.LocalDateTime.now());
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
    public GradeVO inputGradeForAutoCreate(GradeInputDTO inputDTO, Long graderId) {
        log.info("自动创建成绩记录：学生={}, 题目={}, 类型={}, 审核人={}", 
                graderId, inputDTO.getStudentId(), inputDTO.getTopicId(), inputDTO.getGradeType(), graderId);
        
        // 自动创建时不设置评分时间，由教师正式评分时设置
        return inputGradeInternal(inputDTO, graderId, false, false);
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
                       .eq(BizGrade::getIsDeleted, 0);
            
            if (count(existWrapper) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该类型成绩已存在，请勿重复录入");
            }
        }
        
        // 4. 准备分数值（直接使用 DTO 中的分数）
        BigDecimal finalScore = inputDTO.getScore();
        
        // 使用计算器服务验证成绩（仅在有分数时）
        Boolean isPassing = null;
        String gradeLevel = null;
        if (finalScore != null) {
            isPassing = gradeCalculatorService.isPassing(finalScore);
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
    public boolean tryAutoSaveCompositeGrade(Long studentId, Long topicId, Long graderId) {
        log.info("检查是否可以自动保存综合成绩：studentId={}, topicId={}", studentId, topicId);
        
        // 1. 查询该学生该题目的所有成绩
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getStudentId, studentId)
               .eq(BizGrade::getTopicId, topicId)
               .eq(BizGrade::getIsDeleted, 0);
        
        List<BizGrade> grades = list(wrapper);
        
        // 2. 检查是否三种类型成绩都存在且有分数
        boolean hasProposal = grades.stream()
                .anyMatch(g -> g.getGradeType() != null && g.getGradeType() == GradeType.PROPOSAL_GRADE.getCode() && g.getScore() != null);
        boolean hasMidterm = grades.stream()
                .anyMatch(g -> g.getGradeType() != null && g.getGradeType() == GradeType.MIDTERM_GRADE.getCode() && g.getScore() != null);
        boolean hasThesis = grades.stream()
                .anyMatch(g -> g.getGradeType() != null && g.getGradeType() == GradeType.THESIS_GRADE.getCode() && g.getScore() != null);
        
        // 3. 如果三种成绩都存在，计算并保存综合成绩
        if (hasProposal && hasMidterm && hasThesis) {
            log.info("三种成绩类型已完成，开始计算综合成绩：studentId={}, topicId={}", studentId, topicId);
            
            // 检查是否已存在综合成绩
            LambdaQueryWrapper<BizGrade> compositeWrapper = new LambdaQueryWrapper<>();
            compositeWrapper.eq(BizGrade::getStudentId, studentId)
                           .eq(BizGrade::getTopicId, topicId)
                           .eq(BizGrade::getGradeType, GradeType.COMPOSITE_GRADE.getCode()) // 综合成绩
                           .eq(BizGrade::getIsDeleted, 0);
            
            long count = count(compositeWrapper);
            
            if (count > 0) {
                log.info("综合成绩已存在，跳过自动保存：studentId={}, topicId={}", studentId, topicId);
                return false;
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
                
                return true;
            }
        } else {
            log.debug("三种成绩类型未全部完成，暂不计算综合成绩：开题={}, 中期={}, 论文={}", 
                    hasProposal, hasMidterm, hasThesis);
        }
        
        return false;
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
               .eq(BizGrade::getIsDeleted, 0)
               .orderByAsc(BizGrade::getGradeType); // 按成绩类型排序
        
        List<BizGrade> grades = list(wrapper);
        if (grades.isEmpty()) {
            return null;
        }
        
        // 2. 组合各项评语
        StringBuilder compositeComment = new StringBuilder();
        
        // 开题报告评语
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType() == GradeType.PROPOSAL_GRADE.getCode())
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
                .filter(g -> g.getGradeType() != null && g.getGradeType() == GradeType.MIDTERM_GRADE.getCode())
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
                .filter(g -> g.getGradeType() != null && g.getGradeType() == GradeType.THESIS_GRADE.getCode())
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
               .eq(BizGrade::getIsDeleted, 0);
        
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
                .filter(g -> g.getGradeType() != null && g.getGradeType() == GradeType.PROPOSAL_GRADE.getCode()) // 开题报告教师评分
                .findFirst()
                .ifPresent(proposalGrade -> {
                    scores.add(proposalGrade.getScore());
                    weights.add(new BigDecimal("0.3"));
                });
        
        // 中期报告评分权重 0.3
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType() == GradeType.MIDTERM_GRADE.getCode()) // 中期报告教师评分
                .findFirst()
                .ifPresent(midtermGrade -> {
                    scores.add(midtermGrade.getScore());
                    weights.add(new BigDecimal("0.3"));
                });
        
        // 毕业论文评分权重 0.4
        grades.stream()
                .filter(g -> g.getGradeType() != null && g.getGradeType() == GradeType.THESIS_GRADE.getCode()) // 毕业论文教师评分
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
               .eq(BizGrade::getIsDeleted, 0)
               .orderByDesc(BizGrade::getGradedAt);
        
        return list(wrapper).stream()
                .map(this::convertToGradeVO)
                .toList();
    }

    @Override
    public List<GradeVO> getGradesByTeacher(Long teacherId) {
        LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizGrade::getGraderId, teacherId)
               .eq(BizGrade::getIsDeleted, 0)
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
}