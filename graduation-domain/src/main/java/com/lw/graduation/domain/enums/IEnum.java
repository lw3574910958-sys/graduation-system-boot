package com.lw.graduation.domain.enums;


/**
 * 通用枚举接口，所有业务枚举应实现此接口
 */
public interface IEnum<T> {

    /**
     * 获取枚举的 code（唯一标识）
     */
    T getCode();

    /**
     * 获取枚举的描述（用于展示）
     */
    String getDescription();
}
