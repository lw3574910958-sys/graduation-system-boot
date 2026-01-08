package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum FileType {

    /**
     * 状态枚举
     */
    PROPOSAL(0, "开题报告"),
    MIDTERM(1, "中期报告"),
    THESIS(2, "毕业论文");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

}
