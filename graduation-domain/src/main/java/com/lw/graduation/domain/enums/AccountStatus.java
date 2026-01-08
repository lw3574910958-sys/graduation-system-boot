package com.lw.graduation.domain.enums;

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

}
