package com.lw.graduation.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.api.dto.dashboard.GradeDistributionVO;
import com.lw.graduation.api.dto.dashboard.TopicProgressVO;
import com.lw.graduation.api.service.dashboard.DashboardStatisticsService;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.status.SelectionStatus;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
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

    @Override
    public GradeDistributionVO getGradeDistribution(Integer year) {
        log.info("获取成绩分布统计，年份：{}", year);
        return calculateGradeDistribution(year);
    }

    /**
     * 计算成绩分布数据
     */
    private GradeDistributionVO calculateGradeDistribution(Integer year) {
        // 1. 查询指定年份的所有成绩记录
        LambdaQueryWrapper<BizGrade> gradeWrapper = new LambdaQueryWrapper<>();
        if (year != null) {
            LocalDateTime startTime = LocalDateTime.of(year, 1, 1, 0, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(year, 12, 31, 23, 59, 59);
            gradeWrapper.between(BizGrade::getCreatedAt, startTime, endTime);
        }
        gradeWrapper.eq(BizGrade::getIsDeleted, 0);

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
        log.info("获取选题进度统计，院系 ID: {}", departmentId);
        return calculateTopicProgress(departmentId);
    }

    @Override
    public List<Integer> getAvailableGradeYears() {
        log.info("获取可用的成绩年份列表");
        // 1. 查询所有成绩记录，按年份分组
        LambdaQueryWrapper<BizGrade> gradeWrapper = new LambdaQueryWrapper<>();
        gradeWrapper.select(BizGrade::getCreatedAt)
                   .eq(BizGrade::getIsDeleted, 0);
        
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
        topicWrapper.eq(BizTopic::getIsDeleted, 0);
    
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
                               .eq(BizSelection::getIsDeleted, 0);
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
}
