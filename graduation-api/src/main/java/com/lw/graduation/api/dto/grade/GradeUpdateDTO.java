package com.lw.graduation.api.dto.grade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新成绩 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "更新成绩参数")
public class GradeUpdateDTO {

    /**
     * 学生ID
     */
    @Schema(description = "学生ID")
    private Long studentId;

    /**
     * 课题ID
     */
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