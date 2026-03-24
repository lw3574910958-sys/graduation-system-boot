package com.lw.graduation.domain.enums.topic;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目工作量枚举 (1-5)
 */
@Getter
@AllArgsConstructor
public enum TopicWorkload  implements IEnum<Integer>  {
    
    LESS_THAN_10_HOURS(1, "少于10学时"),
    TEN_TO_TWENTY_HOURS(2, "10-20学时"),
    TWENTY_TO_THIRTY_HOURS(3, "20-30学时"),
    THIRTY_TO_FORTY_HOURS(4, "30-40学时"),
    MORE_THAN_FORTY_HOURS(5, "40学时以上");


    /**
     * Code（数据库存储值）
     */
    private final Integer code;
    
    /**
     * 描述
     */
    private final String description;
}
