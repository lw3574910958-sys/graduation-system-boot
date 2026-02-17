package com.lw.graduation.domain.entity.notice;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告实体类
 * 用于管理系统发布的各类通知和公告信息。
 *
 * @author lw
 */
@Data
@TableName("biz_notice")
public class BizNotice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 通知类型: 1-系统通知, 2-公告, 3-提醒
     */
    @TableField("type")
    private Integer type;

    /**
     * 优先级: 1-低, 2-中, 3-高
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 发布者ID(sys_user.id)
     */
    @TableField("publisher_id")
    private Long publisherId;

    /**
     * 发布时间
     */
    @TableField("published_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    /**
     * 生效开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 状态: 0-草稿, 1-已发布, 2-已撤回
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否置顶: 0-否, 1-是
     */
    @TableField("is_sticky")
    private Integer isSticky;

    /**
     * 阅读次数
     */
    @TableField("read_count")
    private Integer readCount;

    /**
     * 目标范围: 0-全体, 1-学生, 2-教师, 3-管理员
     */
    @TableField("target_scope")
    private Integer targetScope;

    /**
     * 附件URL
     */
    @TableField("attachment_url")
    private String attachmentUrl;

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
     * 检查通知是否已发布
     *
     * @return 已发布返回true
     */
    public boolean isPublished() {
        return this.status != null && this.status == 1;
    }

    /**
     * 检查通知是否为草稿
     *
     * @return 草稿返回true
     */
    public boolean isDraft() {
        return this.status == null || this.status == 0;
    }

    /**
     * 检查通知是否已撤回
     *
     * @return 已撤回返回true
     */
    public boolean isWithdrawn() {
        return this.status != null && this.status == 2;
    }

    /**
     * 检查通知是否置顶
     *
     * @return 置顶返回true
     */
    public boolean isSticky() {
        return this.isSticky != null && this.isSticky == 1;
    }

    /**
     * 检查通知是否在有效期内
     *
     * @return 有效返回true
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        if (this.startTime != null && now.isBefore(this.startTime)) {
            return false;
        }
        if (this.endTime != null && now.isAfter(this.endTime)) {
            return false;
        }
        return true;
    }
}
