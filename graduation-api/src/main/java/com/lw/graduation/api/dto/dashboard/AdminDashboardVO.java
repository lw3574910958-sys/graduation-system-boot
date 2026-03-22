package com.lw.graduation.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员仪表盘统计信息
 *
 * @author lw
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员仪表盘统计信息")
public class AdminDashboardVO {

    /**
     * 待审核题目数量
     */
    @Schema(description = "待审核题目数量")
    private Integer pendingTopics;

    /**
     * 学生总数
     */
    @Schema(description = "学生总数")
    private Integer totalStudents;

    /**
     * 教师总数
     */
    @Schema(description = "教师总数")
    private Integer totalTeachers;

    /**
     * 已选题学生数
     */
    @Schema(description = "已选题学生数")
    private Integer selectedStudents;

    /**
     * 未选题学生数
     */
    @Schema(description = "未选题学生数")
    private Integer unselectedStudents;

    /**
     * 院系总数
     */
    @Schema(description = "院系总数")
    private Integer totalDepartments;

    /**
     * 总题目数
     */
    @Schema(description = "总题目数")
    private Integer totalTopics;
}
