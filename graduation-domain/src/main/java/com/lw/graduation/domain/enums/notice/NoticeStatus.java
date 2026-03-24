package com.lw.graduation.domain.enums.notice;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum NoticeStatus implements IEnum<Integer> {

    /**
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 已发布
     */
    PUBLISHED(1, "已发布"),

    /**
     * 已撤回
     */
    WITHDRAWN(2, "已撤回");

    /**
     * Code（数据库存储值）
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

    

    /**
     * 判断是否为最终状态
     *
     * @return 最终状态返回true
     */
    public boolean isFinalStatus() {
        return this == PUBLISHED || this == WITHDRAWN;
    }

    /**
     * 判断是否可以编辑
     *
     * @return 可以编辑返回true
     */
    public boolean canEdit() {
        return this == DRAFT;
    }

    /**
     * 判断是否可以发布
     *
     * @return 可以发布返回true
     */
    public boolean canPublish() {
        return this == DRAFT;
    }

    /**
     * 判断是否可以撤回
     *
     * @return 可以撤回返回true
     */
    public boolean canWithdraw() {
        return this == PUBLISHED;
    }
}