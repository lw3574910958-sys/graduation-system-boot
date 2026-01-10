package com.lw.graduation.domain.entity.user;

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
 * 系统用户表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号(学号/工号/管理员账号)
     */
    @TableField("username")
    private String username;

    /**
     * 密码(bcrypt加密)
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 用户类型: student, teacher, admin
     */
    @TableField("user_type")
    private String userType;

    /**
     * 状态: 0-禁用, 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP(支持IPv6)
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 连续登录失败次数
     */
    @TableField("login_fail_count")
    private Integer loginFailCount;

    /**
     * 账户锁定截止时间
     */
    @TableField("locked_until")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedUntil;

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
     * 头像URL或存储路径
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
