package com.lw.graduation.api.service.dashboard;

import com.lw.graduation.api.dto.dashboard.GradeDistributionVO;
import com.lw.graduation.api.dto.dashboard.TopicProgressVO;

/**
 * 仪表盘统计服务接口（图表相关）
 *
 * @author lw
 */
public interface DashboardStatisticsService {

    /**
     * 获取成绩分布统计
     *
     * @param year 年份
     * @return 成绩分布统计
     */
    GradeDistributionVO getGradeDistribution(Integer year);

    /**
     * 获取选题进度统计
     *
     * @param departmentId 院系 ID（可选，null 表示所有院系）
     * @return 选题进度统计
     */
    TopicProgressVO getTopicProgress(Long departmentId);
}
