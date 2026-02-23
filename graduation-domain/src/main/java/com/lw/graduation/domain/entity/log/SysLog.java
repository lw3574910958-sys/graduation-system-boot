package com.lw.graduation.domain.entity.log;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统日志表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名 (用于未认证场景)
     */
    @TableField("username")
    private String username;

    /**
     * 操作人类型
     */
    @TableField("user_type")
    private String userType;

    /**
     * 模块名称 (user/topic/selection/document/grade/admin/security)
     */
    @TableField("module")
    private String module;

    /**
     * 操作描述
     */
    @TableField("operation")
    private String operation;

    /**
     * 业务关联ID
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * 操作状态: 0-失败, 1-成功, 2-进行中
     */
    @TableField("status")
    private Integer status;

    /**
     * 操作IP
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 操作耗时(毫秒)
     */
    @TableField("duration_ms")
    private Integer durationMs;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
