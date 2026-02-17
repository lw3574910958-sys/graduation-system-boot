package com.lw.graduation.api.vo.notice;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知信息视图对象 (View Object)
 * 用于向外部（如前端）展示通知的详细信息。
 * 对应领域层的 BizNotice 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "通知信息视图对象")
public class NoticeVO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知标题
     */
    @Schema(description = "通知标题")
    private String title;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容")
    private String content;

    /**
     * 通知类型
     */
    @Schema(description = "通知类型")
    private Integer type;

    /**
     * 通知类型描述
     */
    @Schema(description = "通知类型描述")
    private String typeDesc;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 优先级描述
     */
    @Schema(description = "优先级描述")
    private String priorityDesc;

    /**
     * 发布者ID
     */
    @Schema(description = "发布者ID")
    private Long publisherId;

    /**
     * 发布者姓名
     */
    @Schema(description = "发布者姓名")
    private String publisherName;

    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime publishedAt;

    /**
     * 生效开始时间
     */
    @Schema(description = "生效开始时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @Schema(description = "生效结束时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime endTime;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶")
    private Integer isSticky;

    /**
     * 阅读次数
     */
    @Schema(description = "阅读次数")
    private Integer readCount;

    /**
     * 目标范围
     */
    @Schema(description = "目标范围")
    private Integer targetScope;

    /**
     * 目标范围描述
     */
    @Schema(description = "目标范围描述")
    private String targetScopeDesc;

    /**
     * 附件URL
     */
    @Schema(description = "附件URL")
    private String attachmentUrl;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;
}