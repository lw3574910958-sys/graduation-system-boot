package com.lw.graduation.log.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志实体类
 * 对应数据库表 sys_log_enhanced
 * 
 * 基于增强版表结构设计，支持完整的日志管理功能
 *
 * @author lw
 */
@Data
@TableName("sys_log_enhanced")
public class SysLog {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作人ID (允许NULL，用于安全事件)
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 用户名 (用于未认证场景)
     */
    @TableField("username")
    private String username;
    
    /**
     * 操作人类型 (student/teacher/admin/anonymous)
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
     * 操作IP地址
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
    private LocalDateTime createdAt;
}