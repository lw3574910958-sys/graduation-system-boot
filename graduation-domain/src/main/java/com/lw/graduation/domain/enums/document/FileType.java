package com.lw.graduation.domain.enums.document;

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
public enum FileType {

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
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取文件类型枚举
     *
     * @param value 文件类型值
     * @return 对应的枚举，未找到返回null
     */
    public static FileType getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        
        for (FileType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     *
     * @param value 文件类型值
     * @return 有效返回true
     */
    public static boolean isValid(Integer value) {
        return getByValue(value) != null;
    }

}
