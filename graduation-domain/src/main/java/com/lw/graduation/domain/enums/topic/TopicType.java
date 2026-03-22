package com.lw.graduation.domain.enums.topic;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目类型枚举
 * 定义毕业设计题目的研究类型
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicType implements IEnum<String> {

    /**
     * 理论研究 - 侧重于理论分析和学术研究
     */
    THEORETICAL_RESEARCH("理论研究", "理论研究"),

    /**
     * 应用开发 - 侧重于实际应用和系统开发
     */
    APPLIED_RESEARCH("应用开发", "应用开发"),

    /**
     * 其他 - 其他类型
     */
    OTHER("其他", "其他");

    /**
     * Code(数据库存储值)
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

}
