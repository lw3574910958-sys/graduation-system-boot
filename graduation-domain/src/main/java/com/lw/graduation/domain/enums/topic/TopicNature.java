package com.lw.graduation.domain.enums.topic;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目性质枚举
 * 定义毕业设计题目的工作性质
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicNature implements IEnum<String> {

    /**
     * 工程设计 - 侧重于工程设计和实现
     */
    ENGINEERING_DESIGN("工程设计", "工程设计"),

    /**
     * 科学研究 - 侧重于科学研究和探索
     */
    SCIENTIFIC_RESEARCH("科学研究", "科学研究"),

    /**
     * 其他 - 其他性质
     */
    OTHER("其他", "其他");

    /**
     * Code（数据库存储值）
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

}
