package com.lw.graduation.api.vo.grade;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 成绩信息视图对象 (View Object)
 * 用于向外部（如前端）展示成绩的详细信息。
 * 对应领域层的 BizGrade 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "成绩信息视图对象")
public class GradeVO {

    /**
     * 成绩ID
     */
    private Long id;

    /**
     * 学生ID
     */
    @Schema(description = "学生ID")
    private Long studentId;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 课题ID
     */
    @Schema(description = "课题ID")
    private Long topicId;

    /**
     * 课题标题
     */
    @Schema(description = "课题标题")
    private String topicTitle;

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

    /**
     * 评分时间
     */
    @Schema(description = "评分时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime gradedAt;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD) // 格式化时间输出
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNumber;

    /**
     * 成绩等级
     */
    @Schema(description = "成绩等级")
    private String gradeLevel;

    /**
     * 绩点
     */
    @Schema(description = "绩点")
    private java.math.BigDecimal gpa;

    /**
     * 是否及格
     */
    @Schema(description = "是否及格")
    private Boolean passing;

    /**
     * 是否优秀
     */
    @Schema(description = "是否优秀")
    private Boolean excellent;

    /**
     * 评分教师姓名
     */
    @Schema(description = "评分教师姓名")
    private String graderName;
}