package com.lw.graduation.domain.enums.user;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 * 与 sys_user.user_type 字段值一致
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum UserType implements IEnum<String> {
    /**
     * 登录用户枚举
     */
    ADMIN("admin", "管理员"), // 兼容旧数据
    STUDENT("student", "学生"),
    TEACHER("teacher", "教师"),
    SYSTEM_ADMIN("system_admin", "系统管理员"),
    DEPARTMENT_ADMIN("department_admin", "院系管理员");

    /**
     * 状态码
     */
    private final String code;
    /**
     * 描述
     */
    private final String description;
}