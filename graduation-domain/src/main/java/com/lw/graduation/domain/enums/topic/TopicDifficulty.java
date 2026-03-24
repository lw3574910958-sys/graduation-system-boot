package com.lw.graduation.domain.enums.topic;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目难度枚举 (1-5)
 */
@Getter
@AllArgsConstructor
public enum TopicDifficulty implements IEnum<Integer> {

    EASY(1, "简单"),
    MIDDLE(2, "中等"),
    HARD(3, "困难"),
    VERY_HARD(4, "非常困难"),
    EXTREME(5, "极端困难");

    /**
     * Code（数据库存储值）
     */
    private final Integer code;
    
    /**
     * 描述
     */
    private final String description;
    
}
