package com.lw.graduation.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师仪表盘统计信息
 *
 * @author lw
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "教师仪表盘统计信息")
public class TeacherDashboardVO {

    /**
     * 待审核题目数量
     */
    @Schema(description = "待审核题目数量")
    private Integer pendingTopics;

    /**
     * 已发布题目总数
     */
    @Schema(description = "已发布题目总数")
    private Integer totalTopics;

    /**
     * 待审核选题申请数量
     */
    @Schema(description = "待审核选题申请数量")
    private Integer pendingSelections;

    /**
     * 待审核文档数量
     */
    @Schema(description = "待审核文档数量")
    private Integer pendingDocuments;

    /**
     * 指导学生总数
     */
    @Schema(description = "指导学生总数")
    private Integer totalStudents;

    /**
     * 已确认选题数量
     */
    @Schema(description = "已确认选题数量")
    private Integer confirmedSelections;
}
