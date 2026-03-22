package com.lw.graduation.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘统计信息 DTO
 *
 * @author lw
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表盘统计信息")
public class DashboardStatisticsDTO {

    /**
     * 待办事项数量
     */
    @Schema(description = "待办事项数量")
    private Integer pendingCount;

    /**
     * 已完成数量
     */
    @Schema(description = "已完成数量")
    private Integer completedCount;

    /**
     * 进行中数量
     */
    @Schema(description = "进行中数量")
    private Integer inProgressCount;

    /**
     * 总数量
     */
    @Schema(description = "总数量")
    private Integer totalCount;
}
