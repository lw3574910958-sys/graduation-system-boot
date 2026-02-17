package com.lw.graduation.domain.enums.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum NoticeType {

    /**
     * 系统通知
     */
    SYSTEM_NOTICE(1, "系统通知"),

    /**
     * 公告
     */
    ANNOUNCEMENT(2, "公告"),

    /**
     * 提醒
     */
    REMINDER(3, "提醒");

    /**
     * 值
     */
    private final Integer value;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取通知类型枚举
     *
     * @param value 类型值
     * @return 对应的枚举，未找到返回null
     */
    public static NoticeType getByValue(Integer value) {
        if (value == null) {
            return null;
        }

        for (NoticeType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     *
     * @param value 类型值
     * @return 有效返回true
     */
    public static boolean isValid(Integer value) {
        return getByValue(value) != null;
    }
}