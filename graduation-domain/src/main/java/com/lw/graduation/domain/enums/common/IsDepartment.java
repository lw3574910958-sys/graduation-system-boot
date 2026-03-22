package com.lw.graduation.domain.enums.common;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IsDepartment implements IEnum<Integer> {

    /**
     * 非院系管理员 (系统管理员)
     */
    NOT_DEPARTMENT(0, "系统管理员"),
    
    /**
     * 院系管理员
     */
    DEPARTMENT(1, "院系管理员");

    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 描述
     */
    private final String description;

}