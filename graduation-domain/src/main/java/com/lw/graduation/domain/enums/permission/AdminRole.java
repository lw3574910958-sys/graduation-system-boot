package com.lw.graduation.domain.enums.permission;

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
public enum AdminRole {
    
    /**
     * 系统管理员 - 拥有最高权限，可管理所有用户和系统配置
     */
    SYSTEM_ADMIN(0, "system_admin", "系统管理员"),
    
    /**
     * 院系管理员 - 管理特定院系的师生信息和毕业设计流程
     */
    DEPARTMENT_ADMIN(1, "department_admin", "院系管理员");

    /**
     * 数据库存储值
     */
    private final Integer value;
    
    /**
     * 角色编码（用于sys_user_role表）
     */
    private final String code;
    
    /**
     * 角色描述
     */
    private final String description;

    /**
     * 根据数据库值获取枚举
     *
     * @param value 数据库存储值
     * @return 对应的枚举，未找到返回null
     */
    public static AdminRole getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        
        for (AdminRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * 根据角色编码获取枚举
     *
     * @param code 角色编码
     * @return 对应的枚举，未找到返回null
     */
    public static AdminRole getByCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (AdminRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
    
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
     * @return 是管理员返回true
     */
    public boolean isAdmin() {
        return true; // 所有枚举值都是管理员角色
    }
}