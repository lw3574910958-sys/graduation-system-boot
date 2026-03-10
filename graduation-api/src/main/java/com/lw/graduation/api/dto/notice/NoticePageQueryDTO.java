package com.lw.graduation.api.dto.notice;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 通知分页查询DTO
 * 用于通知分页查询的数据传输对象
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知分页查询DTO")
public class NoticePageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通知标题模糊查询
     */
    @Schema(description = "通知标题")
    private String title;

    /**
     * 通知类型
     */
    @Schema(description = "通知类型")
    private Integer type;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 是否置顶
     */
    @Schema(description = "是否置顶")
    private Integer isSticky;

    /**
     * 目标范围
     */
    @Schema(description = "目标范围")
    private Integer targetScope;

    /**
     * 发布者 ID
     */
    @Schema(description = "发布者 ID")
    private Long publisherId;

    /**
     * 当前用户类型（用于过滤目标范围）
     * 0-学生，1-教师，2-管理员
     */
    @Schema(description = "当前用户类型（用于过滤目标范围）")
    private Integer currentUserType;

    /**
     * 生效状态过滤
     * effective-生效中，pending-待生效，expired-已过期
     */
    @Schema(description = "生效状态过滤")
    private String effectiveStatus;
}