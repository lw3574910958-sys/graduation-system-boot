package com.lw.graduation.domain.enums.common;

import com.lw.graduation.domain.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否有部门
 */
@Getter
@AllArgsConstructor
public enum IsDepartment implements IEnum<Integer> {

    NOT_DEPARTMENT(0, "无部门"),
    DEPARTMENT(1, "有部门");

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;

}
