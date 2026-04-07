package com.lw.graduation.dashboard.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.api.dto.dashboard.GradeDistributionVO;
import com.lw.graduation.api.dto.dashboard.TopicProgressVO;
import com.lw.graduation.api.service.dashboard.DashboardStatisticsService;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.domain.enums.status.SelectionStatus;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表盘统计服务实现（图表相关）
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardStatisticsServiceImpl implements DashboardStatisticsService {

    private final BizGradeMapper bizGradeMapper;
    private final BizTopicMapper bizTopicMapper;
    private final BizSelectionMapper bizSelectionMapper;
    private final BizStudentMapper bizStudentMapper;
    private final DataPermissionUtil dataPermissionUtil;

    @Override
    public GradeDistributionVO getGradeDistribution(Integer year) {
        log.info("获取成绩分布统计，年份：{}", year);
        
        // 获取当前登录用户的院系 ID（用于院系管理员数据隔离）
        Long departmentId = getCurrentUserDepartmentId();
        
        return calculateGradeDistribution(year, departmentId);
    }

    /**
     * 计算成绩分布数据
     *
     * @param year 年份
     * @param departmentId 院系 ID（null 表示全局统计）
     */
    private GradeDistributionVO calculateGradeDistribution(Integer year, Long departmentId) {
        // 1. 查询指定年份的所有成绩记录（根据院系 ID 过滤）
        LambdaQueryWrapper<BizGrade> gradeWrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            LocalDateTime startTime = LocalDateTime.of(year, 1, 1, 0, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            gradeWrapper.between(BizGrade::getCreatedAt, startTime, endTime);
        }
        gradeWrapper.eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());

        // 如果是院系管理员，需要通过学生表关联过滤本院系
        if (departmentId != null) {
            // 先查询本院系所有学生 ID
            LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(BizStudent::getDepartmentId, departmentId)
                         .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            List<BizStudent> deptStudents = bizStudentMapper.selectList(studentWrapper);
            
            if (deptStudents.isEmpty()) {
                // 本院系没有学生，直接返回空统计
                return GradeDistributionVO.builder()
                    .excellent(0)
                    .good(0)
                    .medium(0)
                    .pass(0)
                    .fail(0)
                    .total(0)
                    .build();
            }
            
            // 提取学生 ID 列表
            List<Long> studentIds = deptStudents.stream()
                .map(BizStudent::getId)
                .toList();
            
            // 只统计这些学生的成绩
            gradeWrapper.in(BizGrade::getStudentId, studentIds);
        }

        List<BizGrade> grades = bizGradeMapper.selectList(gradeWrapper);

        // 2. 统计各等级人数
        long excellent = 0;  // 90-100
        long good = 0;       // 80-89
        long medium = 0;     // 70-79
        long pass = 0;       // 60-69
        long fail = 0;       // <60

        for (BizGrade grade : grades) {
            BigDecimal score = grade.getScore();
            if (score == null) continue;

            if (score.compareTo(new BigDecimal("90")) >= 0) {
                excellent++;
            } else if (score.compareTo(new BigDecimal("80")) >= 0) {
                good++;
            } else if (score.compareTo(new BigDecimal("70")) >= 0) {
                medium++;
            } else if (score.compareTo(new BigDecimal("60")) >= 0) {
                pass++;
            } else {
                fail++;
            }
        }

        return GradeDistributionVO.builder()
            .excellent((int) excellent)
            .good((int) good)
            .medium((int) medium)
            .pass((int) pass)
            .fail((int) fail)
            .total(grades.size())
            .build();
    }

    @Override
    public TopicProgressVO getTopicProgress(Long departmentId) {
        log.info("获取选题进度统计，传入院系 ID: {}", departmentId);
        
        // 获取当前登录用户的院系 ID（用于院系管理员数据隔离）
        Long currentUserDeptId = getCurrentUserDepartmentId();
        
        // 如果是院系管理员，强制使用其本院系 ID，忽略传入的参数
        // 系统管理员使用传入的参数（支持前端下拉选择过滤）
        Long effectiveDeptId = (currentUserDeptId != null) ? currentUserDeptId : departmentId;
        
        log.info("实际使用院系 ID: {}", effectiveDeptId);
        return calculateTopicProgress(effectiveDeptId);
    }

    @Override
    public List<Integer> getAvailableGradeYears() {
        log.info("获取可用的成绩年份列表");
        
        // 获取当前登录用户的院系 ID（用于院系管理员数据隔离）
        Long departmentId = getCurrentUserDepartmentId();
        
        // 1. 查询所有成绩记录，按年份分组
        LambdaQueryWrapper<BizGrade> gradeWrapper = new LambdaQueryWrapper<>();
        gradeWrapper.select(BizGrade::getCreatedAt)
                   .eq(BizGrade::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        
        // 如果是院系管理员，需要通过学生表关联过滤本院系
        if (departmentId != null) {
            LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(BizStudent::getDepartmentId, departmentId)
                         .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            List<BizStudent> deptStudents = bizStudentMapper.selectList(studentWrapper);
            
            if (deptStudents.isEmpty()) {
                return List.of();
            }
            
            List<Long> studentIds = deptStudents.stream()
                .map(BizStudent::getId)
                .toList();
            
            gradeWrapper.in(BizGrade::getStudentId, studentIds);
        }
        
        List<BizGrade> grades = bizGradeMapper.selectList(gradeWrapper);
        
        // 2. 提取年份并去重
        return grades.stream()
            .map(grade -> grade.getCreatedAt().getYear())
            .distinct()
            .sorted((a, b) -> b - a) // 降序排列，最近的年份在前
            .toList();
    }

    /**
     * 计算选题进度数据
     */
    private TopicProgressVO calculateTopicProgress(Long departmentId) {
        // 1. 查询所有题目（根据院系 ID 过滤）
        LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
        if (departmentId != null) {
            topicWrapper.eq(BizTopic::getDepartmentId, departmentId);
        }
        topicWrapper.eq(BizTopic::getIsDeleted, IsDelete.NOT_DELETED.getCode());
    
        List<BizTopic> allTopics = bizTopicMapper.selectList(topicWrapper);
    
        // 2. 统计各状态题目数量
        long open = allTopics.stream()
            .filter(t -> t.getStatus().equals(TopicStatus.OPEN.getCode()))
            .count();
    
        long reviewing = allTopics.stream()
            .filter(t -> t.getStatus().equals(TopicStatus.REVIEWING.getCode()))
            .count();
    
        long selected = allTopics.stream()
            .filter(t -> {
                // 检查该题目是否有已确认的选题
                LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
                selectionWrapper.eq(BizSelection::getTopicId, t.getId())
                               .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getCode())
                               .eq(BizSelection::getIsDeleted, IsDelete.NOT_DELETED.getCode());
                return bizSelectionMapper.selectCount(selectionWrapper) > 0;
            })
            .count();
    
        long closed = allTopics.stream()
            .filter(t -> t.getStatus().equals(TopicStatus.CLOSED.getCode()))
            .count();
    
        long total = allTopics.size();
    
        return TopicProgressVO.builder()
            .open((int) open)
            .reviewing((int) reviewing)
            .selected((int) selected)
            .closed((int) closed)
            .total((int) total)
            .build();
    }

    /**
     * 获取当前登录用户的院系 ID（用于院系管理员数据隔离）
     * 系统管理员返回 null（全局统计）
     */
    private Long getCurrentUserDepartmentId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Long userId = StpUtil.getLoginIdAsLong();
        return dataPermissionUtil.getDepartmentIdByUserId(userId);
    }
}
