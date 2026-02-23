package com.lw.graduation.api.vo.selection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选题信息视图对象 (View Object)
 * 用于向外部（如前端）展示选题的详细信息。
 * 对应领域层的 BizSelection 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "选题信息视图对象")
public class SelectionVO {

    /**
     * 选题ID
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
     * 状态 (0-待审核, 1-审核通过, 2-审核驳回, 3-已确认)
     */
    @Schema(description = "状态 (0-待审核, 1-审核通过, 2-审核驳回, 3-已确认)")
    private Integer status;

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
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 审核教师ID
     */
    @Schema(description = "审核教师ID")
    private Long reviewerId;

    /**
     * 审核教师姓名
     */
    @Schema(description = "审核教师姓名")
    private String reviewerName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime reviewedAt;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComment;

    /**
     * 学生确认时间
     */
    @Schema(description = "学生确认时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime confirmedAt;
}