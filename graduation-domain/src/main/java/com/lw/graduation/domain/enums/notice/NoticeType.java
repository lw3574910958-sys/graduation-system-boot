package com.lw.graduation.domain.enums.notice;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum NoticeType implements IEnum<Integer> {

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
     * Code（数据库存储值）
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

    
}