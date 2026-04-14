package com.lw.graduation.domain.entity.topic;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 题目表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Data
@TableName("biz_topic")
public class BizTopic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 题目标题
     */
    @TableField("title")
    private String title;

    /**
     * 题目描述
     */
    @TableField("description")
    private String description;

    /**
     * 发布教师ID(biz_teacher.id)
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 状态: 0-草稿, 1-审核中, 2-开放, 3-关闭
     */
    @TableField("status")
    private Integer status;

    /**
     * 所属院系ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 题目来源
     */
    @TableField("source")
    private String source;

    /**
     * 题目类型
     */
    @TableField("type")
    private String type;

    /**
     * 题目性质
     */
    @TableField("nature")
    private String nature;

    /**
     * 预计难度(1-5)
     */
    @TableField("difficulty")
    private Integer difficulty;

    /**
     * 预计工作量(1-5)
     */
    @TableField("workload")
    private Integer workload;

    /**
     * 选题人数限制
     */
    @TableField("max_selections")
    private Integer maxSelections;

    /**
     * 已选人数
     */
    @TableField("selected_count")
    private Integer selectedCount;

    /**
     * 最近一次审核结果：NULL-未审，1-通过，2-驳回
     */
    @TableField("last_review_outcome")
    private Integer lastReviewOutcome;

    /**
     * 最近一次审核意见
     */
    @TableField("last_review_feedback")
    private String lastReviewFeedback;

    /**
     * 审核人 ID(sys_user.id)
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 最后一次审核时间
     */
    @TableField("reviewed_at")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime reviewedAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
