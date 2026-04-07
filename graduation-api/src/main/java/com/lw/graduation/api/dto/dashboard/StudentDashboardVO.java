package com.lw.graduation.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学生仪表盘信息
 *
 * @author lw
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学生仪表盘信息")
public class StudentDashboardVO {

    /**
     * 当前流程步骤（0-未选题，1-已选题，2-开题通过，3-中期通过，4-论文通过）
     */
    @Schema(description = "当前流程步骤")
    private Integer currentStep;

    /**
     * 选题标题
     */
    @Schema(description = "选题标题")
    private String topicTitle;
}
