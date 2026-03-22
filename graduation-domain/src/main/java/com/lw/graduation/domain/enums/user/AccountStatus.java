package com.lw.graduation.domain.enums.user;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum AccountStatus implements IEnum<Integer> {
    /**
     * 状态枚举
     */
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;

    /**
     * 判断账户是否启用
     *
     * @return 启用返回 true
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断账户是否禁用
     *
     * @return 禁用返回 true
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

}