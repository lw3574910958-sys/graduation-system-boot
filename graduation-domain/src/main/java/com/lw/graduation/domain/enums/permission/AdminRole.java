package com.lw.graduation.domain.enums.permission;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 管理员角色枚举
 * 统一管理系统中所有管理员相关的角色定义
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum AdminRole implements IEnum<Integer> {
    
    /**
     * 系统管理员 - 拥有最高权限，可管理所有用户和系统配置
     */
    SYSTEM_ADMIN(0, "system_admin", "系统管理员"),
    
    /**
     * 院系管理员 - 管理特定院系的师生信息和毕业设计流程
     */
    DEPARTMENT_ADMIN(1, "department_admin", "院系管理员");

    /**
     * Code（数据库存储值）
     */
    private final Integer code;
        
    /**
     * 角色编码（用于 sys_user_role 表）
     */
    private final String roleCode;
        
    /**
     * 角色描述
     */
    private final String description;

    
    
    /**
     * 判断是否为系统管理员
     *
     * @return 是系统管理员返回true
     */
    public boolean isSystemAdmin() {
        return this == SYSTEM_ADMIN;
    }
    
    /**
     * 判断是否为院系管理员
     *
     * @return 是院系管理员返回true
     */
    public boolean isDepartmentAdmin() {
        return this == DEPARTMENT_ADMIN;
    }
    
    /**
     * 判断是否为管理员角色
     *
     * @return 是管理员返回 true
     */
    public boolean isAdmin() {
        return true; // 所有枚举值都是管理员角色
    }
    
    /**
     * 根据角色编码获取 AdminRole 枚举
     * 用于从 sys_user_role 表的 roleCode 字段查找
     *
     * @param roleCode 角色编码（如 "system_admin"）
     * @return 匹配的 AdminRole，不存在返回 null
     */
    public static AdminRole getByRoleCode(String roleCode) {
        if (roleCode == null) {
            return null;
        }
        for (AdminRole role : values()) {
            if (role.roleCode.equals(roleCode)) {
                return role;
            }
        }
        return null;
    }
}