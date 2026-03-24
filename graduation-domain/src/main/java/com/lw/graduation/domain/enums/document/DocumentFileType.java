package com.lw.graduation.domain.enums.document;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 * 定义毕业设计系统中支持的文档类型及其相关属性
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum DocumentFileType implements IEnum<Integer> {

    /**
     * 开题报告
     */
    PROPOSAL(0, "开题报告"),
    
    /**
     * 中期报告
     */
    MIDTERM(1, "中期报告"),
    
    /**
     * 毕业论文
     */
    THESIS(2, "毕业论文"),
    
    /**
     * 外文翻译
     */
    TRANSLATION(3, "外文翻译"),
    
    /**
     * 其他文档
     */
    OTHER(4, "其他文档");

    /**
     * Code（数据库存储值）
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;

    

}
