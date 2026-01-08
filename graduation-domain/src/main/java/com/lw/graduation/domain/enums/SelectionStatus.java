package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 选题状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum SelectionStatus {

    /**
     * 状态枚举
     */
    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;
}
