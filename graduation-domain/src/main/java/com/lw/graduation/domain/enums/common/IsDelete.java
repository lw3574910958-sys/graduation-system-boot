package com.lw.graduation.domain.enums.common;

import com.lw.graduation.domain.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IsDelete implements IEnum<Integer> {

    /**
     * 删除状态
     */
    NOT_DELETED(0, "正常"),
    DELETED(1, "删除");

    /**
     * 状态码
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;

}
