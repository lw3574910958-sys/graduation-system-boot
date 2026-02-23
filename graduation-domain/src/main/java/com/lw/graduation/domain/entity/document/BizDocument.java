package com.lw.graduation.domain.entity.document;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.domain.enums.status.ReviewStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档表
 * 用于管理毕业设计过程中的各类文档，包括开题报告、中期报告、毕业论文等。
 * 支持文档上传、审核、下载等完整功能。
 */
@Data
@TableName("biz_document")
public class BizDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传人ID(sys_user.id)
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 关联题目ID
     */
    @TableField("topic_id")
    private Long topicId;

    /**
     * 文件类型: 0-开题报告, 1-中期报告, 2-毕业论文
     */
    @TableField("file_type")
    private Integer fileType;

    /**
     * 原始文件名
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * 服务器存储路径
     */
    @TableField("stored_path")
    private String storedPath;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 审核状态: 0-待审, 1-通过, 2-驳回
     */
    @TableField("review_status")
    private Integer reviewStatus;

    /**
     * 审核时间
     */
    @TableField("reviewed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    /**
     * 审核人ID(sys_user.id)
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 审核意见
     */
    @TableField("feedback")
    private String feedback;

    /**
     * 上传时间
     */
    @TableField("uploaded_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;

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
     * 获取文件大小的友好显示格式
     *
     * @return 格式化的文件大小字符串
     */
    public String getFileSizeDisplay() {
        if (this.fileSize == null) {
            return "0 B";
        }
        
        long size = this.fileSize;
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取文件扩展名
     *
     * @return 文件扩展名（小写）
     */
    public String getFileExtension() {
        if (this.originalFilename == null) {
            return "";
        }
        
        int lastDotIndex = this.originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < this.originalFilename.length() - 1) {
            return this.originalFilename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查文档是否已通过审核
     *
     * @return 通过审核返回true
     */
    public boolean isApproved() {
        ReviewStatus status = ReviewStatus.getByValue(this.reviewStatus);
        return status == ReviewStatus.APPROVED;
    }

    /**
     * 检查文档是否被驳回
     *
     * @return 被驳回返回true
     */
    public boolean isRejected() {
        ReviewStatus status = ReviewStatus.getByValue(this.reviewStatus);
        return status == ReviewStatus.REJECTED;
    }

    /**
     * 检查文档是否待审核
     *
     * @return 待审核返回true
     */
    public boolean isPendingReview() {
        ReviewStatus status = ReviewStatus.getByValue(this.reviewStatus);
        return status == null || status == ReviewStatus.PENDING;
    }
}
