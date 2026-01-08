package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicStatus {

    /**
     * 状态枚举
     */
    OPEN(0, "开放"),
    SELECTED(1, "已选"),
    CLOSED(2, "关闭");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

}
