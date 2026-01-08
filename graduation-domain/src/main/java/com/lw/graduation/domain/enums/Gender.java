package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum Gender {

    /**
     * 状态枚举
     */
    FEMALE(0, "女"),
    MALE(1, "男");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

}
