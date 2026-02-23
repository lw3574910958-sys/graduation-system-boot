package com.lw.graduation.domain.enums.user;

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
public enum UserType {
    /**
     * 登录用户枚举
     */
    STUDENT("student", "学生"),
    TEACHER("teacher", "教师"),
    ADMIN("admin", "管理员");

    /**
     * 用户类型
     */
    private final String code;
    /**
     * 用户类型描述
     */
    private final String description;

    /**
     * 校验用户类型是否有效
     *
     * @param code 用户类型
     * @return 有效返回 true，否则返回 false
     */
    public static boolean isValid(String code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 校验用户类型是否无效（为了向后兼容保留原方法）
     *
     * @param code 用户类型
     * @return 无效返回 true，否则返回 false
     */
    public static boolean isInvalid(String code) {
        return !isValid(code);
    }
}