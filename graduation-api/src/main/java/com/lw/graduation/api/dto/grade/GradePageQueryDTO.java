package com.lw.graduation.api.dto.grade;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 成绩分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "成绩分页查询参数")
public class GradePageQueryDTO extends BasePageQueryDTO {

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
     * 评分教师ID
     */
    @Schema(description = "评分教师ID")
    private Long graderId;

    /**
     * 成绩类型
     */
    @Schema(description = "成绩类型：0-开题报告，1-中期报告，2-毕业论文，3-综合成绩")
    private Integer gradeType;

    /**
     * 最低成绩
     */
    @Schema(description = "最低成绩")
    private BigDecimal minScore;

    /**
     * 最高成绩
     */
    @Schema(description = "最高成绩")
    private BigDecimal maxScore;
}