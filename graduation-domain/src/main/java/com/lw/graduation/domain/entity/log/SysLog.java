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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作人类型
     */
    @TableField("user_type")
    private String userType;

    /**
     * 操作描述
     */
    @TableField("operation")
    private String operation;

    /**
     * 操作IP
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
