package com.lw.graduation.api.dto.grade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建成绩 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "创建成绩参数")
public class GradeCreateDTO {

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private Long studentId;

    /**
     * 课题ID
     */
    @NotNull(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private Long topicId;

    /**
     * 成绩(0.00 ~ 100.00)
     */
    @Schema(description = "成绩(0.00 ~ 100.00)")
    private java.math.BigDecimal score;

    /**
     * 评分教师ID
     */
    @Schema(description = "评分教师ID")
    private Long graderId;

    /**
     * 评语
     */
    @Schema(description = "评语")
    private String comment;
}