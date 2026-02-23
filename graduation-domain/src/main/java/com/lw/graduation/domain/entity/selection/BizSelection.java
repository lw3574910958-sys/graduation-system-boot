package com.lw.graduation.domain.entity.selection;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.domain.enums.status.SelectionStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 选题记录表
 * 用于管理学生的选题申请，包括提交、审核、确认等完整流程。
 * 支持教师审核、学生确认、状态跟踪等功能。
 */
@Data
@TableName("biz_selection")
public class BizSelection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID(biz_student.id)
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 题目ID(biz_topic.id)
     */
    @TableField("topic_id")
    private Long topicId;

    /**
     * 选题时的题目标题快照
     */
    @TableField("topic_title")
    private String topicTitle;

    /**
     * 状态: 0-待审核, 1-审核通过, 2-审核驳回, 3-已确认
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 审核教师ID(sys_user.id)
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @TableField("reviewed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    /**
     * 审核意见
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 学生确认时间
     */
    @TableField("confirmed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedAt;

    /**
     * 检查选题是否已通过审核
     *
     * @return 通过审核返回true
     */
    public boolean isApproved() {
        SelectionStatus status = SelectionStatus.getByValue(this.status);
        return status == SelectionStatus.APPROVED;
    }

    /**
     * 检查选题是否被驳回
     *
     * @return 被驳回返回true
     */
    public boolean isRejected() {
        SelectionStatus status = SelectionStatus.getByValue(this.status);
        return status == SelectionStatus.REJECTED;
    }

    /**
     * 检查选题是否待审核
     *
     * @return 待审核返回true
     */
    public boolean isPendingReview() {
        SelectionStatus status = SelectionStatus.getByValue(this.status);
        return status == null || status == SelectionStatus.PENDING_REVIEW;
    }

    /**
     * 检查选题是否已确认
     *
     * @return 已确认返回true
     */
    public boolean isConfirmed() {
        SelectionStatus status = SelectionStatus.getByValue(this.status);
        return status == SelectionStatus.CONFIRMED;
    }
}
