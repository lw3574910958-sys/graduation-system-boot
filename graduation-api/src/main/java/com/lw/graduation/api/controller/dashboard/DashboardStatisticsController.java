package com.lw.graduation.api.controller.dashboard;

import com.lw.graduation.api.dto.dashboard.GradeDistributionVO;
import com.lw.graduation.api.dto.dashboard.TopicProgressVO;
import com.lw.graduation.api.service.dashboard.DashboardStatisticsService;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘统计控制器
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/dashboard/statistics")
@RequiredArgsConstructor
@Tag(name = "仪表盘统计管理", description = "仪表盘统计图表数据")
public class DashboardStatisticsController {

    private final DashboardStatisticsService dashboardStatisticsService;

    /**
     * 获取成绩分布统计
     */
    @GetMapping("/grade/distribution")
    @Operation(summary = "获取成绩分布统计")
    public Result<GradeDistributionVO> getGradeDistribution(
            @RequestParam(required = false) Integer year) {
        GradeDistributionVO vo = dashboardStatisticsService.getGradeDistribution(year);
        return Result.success(vo);
    }

    /**
     * 获取选题进度统计
     */
    @GetMapping("/topic/progress")
    @Operation(summary = "获取选题进度统计")
    public Result<TopicProgressVO> getTopicProgress(
            @RequestParam(required = false) Long departmentId) {
        TopicProgressVO vo = dashboardStatisticsService.getTopicProgress(departmentId);
        return Result.success(vo);
    }
}
