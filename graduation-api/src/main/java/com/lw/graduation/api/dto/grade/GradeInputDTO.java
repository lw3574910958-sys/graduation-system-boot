package com.lw.graduation.api.dto.grade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成绩录入DTO
 * 用于教师录入学生毕业设计成绩的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "成绩录入请求DTO")
public class GradeInputDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;

    /**
     * 成绩 (0.00 ~ 100.00)
     */
    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0.00", message = "成绩不能小于0分")
    @DecimalMax(value = "100.00", message = "成绩不能大于100分")
    @Schema(description = "成绩 (0.00 ~ 100.00)", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal score;

    /**
     * 评语
     */
    @Schema(description = "评语")
    private String comment;

    /**
     * 指导教师评分 (如果需要分别录入)
     */
    @DecimalMin(value = "0.00", message = "指导教师评分不能小于0分")
    @DecimalMax(value = "100.00", message = "指导教师评分不能大于100分")
    @Schema(description = "指导教师评分")
    private BigDecimal advisorScore;

    /**
     * 答辩评分 (如果需要分别录入)
     */
    @DecimalMin(value = "0.00", message = "答辩评分不能小于0分")
    @DecimalMax(value = "100.00", message = "答辩评分不能大于100分")
    @Schema(description = "答辩评分")
    private BigDecimal defenseScore;

    /**
     * 成绩类型 (1-指导教师评分, 2-答辩评分, 3-综合成绩)
     */
    @Schema(description = "成绩类型: 1-指导教师评分, 2-答辩评分, 3-综合成绩")
    private Integer gradeType = 3;
}