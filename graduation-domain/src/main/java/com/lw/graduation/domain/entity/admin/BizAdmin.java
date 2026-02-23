package com.lw.graduation.domain.entity.admin;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.domain.enums.permission.AdminRole;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Data
@TableName("biz_admin")
public class BizAdmin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联 sys_user.id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 管理员编号
     */
    @TableField("admin_id")
    private String adminId;

    /**
     * 管理院系ID(NULL表示系统管理员)
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 角色级别: 0-系统管理员, 1-院系管理员
     */
    @TableField("role_level")
    private Integer roleLevel;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

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
     * 是否删除: 0-否, 1-是
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 获取管理员角色等级枚举
     *
     * @return AdminRole枚举
     */
    public AdminRole getRoleLevelEnum() {
        return AdminRole.getByValue(this.roleLevel);
    }

    /**
     * 检查是否为系统管理员
     *
     * @return 系统管理员返回true
     */
    public boolean isSystemAdmin() {
        return this.roleLevel != null && this.roleLevel.equals(AdminRole.SYSTEM_ADMIN.getValue());
    }

    /**
     * 检查是否为院系管理员
     *
     * @return 院系管理员返回true
     */
    public boolean isDeptAdmin() {
        return this.roleLevel != null && this.roleLevel.equals(AdminRole.DEPARTMENT_ADMIN.getValue());
    }
}
