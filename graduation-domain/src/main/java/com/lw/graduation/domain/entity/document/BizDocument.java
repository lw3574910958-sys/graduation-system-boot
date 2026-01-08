package com.lw.graduation.domain.entity.document;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文档表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
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
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
