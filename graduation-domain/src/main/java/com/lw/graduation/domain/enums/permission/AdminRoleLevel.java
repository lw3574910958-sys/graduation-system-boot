package com.lw.graduation.domain.enums.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 管理员角色等级枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum AdminRoleLevel {

    /**
     * 角色等级
     */
    SYSTEM_ADMIN(0, "系统管理员"),
    DEPT_ADMIN(1, "院系管理员");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;
}
