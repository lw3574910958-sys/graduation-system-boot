package com.lw.graduation.domain.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum AccountStatus {
    /**
     * 状态枚举
     */
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    /**
     * 状态值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取账户状态枚举
     *
     * @param value 状态值
     * @return 对应的枚举，未找到返回null
     */
    public static AccountStatus getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        
        for (AccountStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     *
     * @param value 状态值
     * @return 有效返回true
     */
    public static boolean isValid(Integer value) {
        return getByValue(value) != null;
    }

    /**
     * 判断账户是否启用
     *
     * @return 启用返回true
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断账户是否禁用
     *
     * @return 禁用返回true
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

}
