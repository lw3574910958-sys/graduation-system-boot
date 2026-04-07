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
     * 学生姓名（模糊查询）
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 学生学号（模糊查询）
     */
    @Schema(description = "学生学号")
    private String studentNumber;

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
     * 评分教师姓名（模糊查询）
     */
    @Schema(description = "评分教师姓名")
    private String graderName;

    /**
     * 评分教师工号（模糊查询）
     */
    @Schema(description = "评分教师工号")
    private String graderWorkNumber;

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

    /**
     * 成绩等级（精确匹配）
     */
    @Schema(description = "成绩等级：优秀、良好、中等、及格、不及格")
    private String gradeLevel;

    /**
     * 绩点（精确匹配）
     */
    @Schema(description = "绩点：4.0、3.0、2.0、1.0、0.0")
    private BigDecimal gpa;
}