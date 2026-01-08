package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum ReviewStatus {

    /**
     * 状态枚举
     */
    PENDING(0, "待审"),
    APPROVED(1, "通过"),
    REJECTED(2, "驳回");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;
}
