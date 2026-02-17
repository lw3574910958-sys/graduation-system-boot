package com.lw.graduation.domain.enums;

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

    /**
     * 获取允许的文件扩展名
     *
     * @return 该类型允许的文件扩展名数组
     */
    public String[] getAllowedExtensions() {
        switch (this) {
            case PROPOSAL:
            case MIDTERM:
                return new String[]{"doc", "docx", "pdf"};
            case THESIS:
                return new String[]{"doc", "docx", "pdf", "tex"};
            case TRANSLATION:
                return new String[]{"doc", "docx", "pdf", "txt"};
            case OTHER:
                return new String[]{"doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "txt"};
            default:
                return new String[]{"doc", "docx", "pdf"};
        }
    }

    /**
     * 检查文件扩展名是否被允许
     *
     * @param extension 文件扩展名
     * @return 允许返回true
     */
    public boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        
        String lowerExt = extension.toLowerCase();
        String[] allowedExts = this.getAllowedExtensions();
        
        for (String allowedExt : allowedExts) {
            if (allowedExt.equals(lowerExt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取最大文件大小限制（字节）
     *
     * @return 最大文件大小
     */
    public long getMaxFileSize() {
        switch (this) {
            case THESIS:
                return 50 * 1024 * 1024; // 50MB
            case TRANSLATION:
                return 10 * 1024 * 1024; // 10MB
            default:
                return 20 * 1024 * 1024; // 20MB
        }
    }

}
