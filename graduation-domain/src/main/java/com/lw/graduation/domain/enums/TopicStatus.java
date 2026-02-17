package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicStatus {

    /**
     * 开放状态 - 题目可供学生选择
     */
    OPEN(1, "开放"),
    
    /**
     * 已选状态 - 题目已被学生选中
     */
    SELECTED(2, "已选"),
    
    /**
     * 关闭状态 - 题目不再接受选题
     */
    CLOSED(3, "关闭");

    /**
     * 状态值
     */
    private final Integer value;
    
    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态值获取枚举
     *
     * @param value 状态值
     * @return 对应的枚举，未找到返回null
     */
    public static TopicStatus getByValue(Integer value) {
        for (TopicStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断状态值是否有效
     *
     * @param value 状态值
     * @return 有效返回true，否则返回false
     */
    public static boolean isValid(Integer value) {
        return getByValue(value) != null;
    }

    /**
     * 判断是否为可选状态
     *
     * @return 可选返回true
     */
    public boolean isSelectable() {
        return this == OPEN;
    }

    /**
     * 判断是否为活跃状态
     *
     * @return 活跃返回true
     */
    public boolean isActive() {
        return this != CLOSED;
    }
}
