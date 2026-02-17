package com.lw.graduation.api.dto.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知更新DTO
 * 用于更新通知的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "通知更新DTO")
public class NoticeUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * 通知类型
     */
    @NotNull(message = "通知类型不能为空")
    @Schema(description = "通知类型: 1-系统通知, 2-公告, 3-提醒", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    /**
     * 优先级
     */
    @Schema(description = "优先级: 1-低, 2-中, 3-高")
    private Integer priority;

    /**
     * 生效开始时间
     */
    @Schema(description = "生效开始时间")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @Schema(description = "生效结束时间")
    private LocalDateTime endTime;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶: 0-否, 1-是")
    private Integer isSticky;

    /**
     * 目标范围
     */
    @Schema(description = "目标范围: 0-全体, 1-学生, 2-教师, 3-管理员")
    private Integer targetScope;

    /**
     * 附件URL
     */
    @Schema(description = "附件URL")
    private String attachmentUrl;
}