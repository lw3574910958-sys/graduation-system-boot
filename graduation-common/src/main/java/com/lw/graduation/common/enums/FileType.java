package com.lw.graduation.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 * 定义系统支持的所有文件类型及其属性
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum FileType {
    
    // 图片类型
    JPG("jpg", "JPEG图片", Category.IMAGE, true, 10 * 1024 * 1024L), // 10MB
    JPEG("jpeg", "JPEG图片", Category.IMAGE, true, 10 * 1024 * 1024L),
    PNG("png", "PNG图片", Category.IMAGE, true, 10 * 1024 * 1024L),
    GIF("gif", "GIF动图", Category.IMAGE, true, 10 * 1024 * 1024L),
    
    // 文档类型
    DOC("doc", "Word文档", Category.DOCUMENT, true, 50 * 1024 * 1024L), // 50MB
    DOCX("docx", "Word文档", Category.DOCUMENT, true, 50 * 1024 * 1024L),
    PDF("pdf", "PDF文档", Category.DOCUMENT, true, 50 * 1024 * 1024L),
    TXT("txt", "文本文件", Category.DOCUMENT, true, 10 * 1024 * 1024L),
    
    // 表格类型
    XLS("xls", "Excel表格", Category.SPREADSHEET, true, 50 * 1024 * 1024L),
    XLSX("xlsx", "Excel表格", Category.SPREADSHEET, true, 50 * 1024 * 1024L),
    
    // 演示文稿类型
    PPT("ppt", "PowerPoint演示文稿", Category.PRESENTATION, true, 50 * 1024 * 1024L),
    PPTX("pptx", "PowerPoint演示文稿", Category.PRESENTATION, true, 50 * 1024 * 1024L),
    
    // 其他类型
    ZIP("zip", "压缩文件", Category.ARCHIVE, false, 100 * 1024 * 1024L), // 100MB
    RAR("rar", "压缩文件", Category.ARCHIVE, false, 100 * 1024 * 1024L);

    /**
     * 文件扩展名（不含点号）
     */
    private final String extension;
    
    /**
     * 文件类型描述
     */
    private final String description;
    
    /**
     * 文件类别
     */
    private final Category category;
    
    /**
     * 是否允许上传
     */
    private final boolean allowed;
    
    /**
     * 最大大小限制（字节）
     */
    private final Long maxSize;

    /**
     * 文件类别枚举
     */
    @Getter
    @AllArgsConstructor
    public enum Category {
        IMAGE("图片"),
        DOCUMENT("文档"),
        SPREADSHEET("电子表格"),
        PRESENTATION("演示文稿"),
        ARCHIVE("压缩文件"),
        OTHER("其他");

        private final String description;
    }

    /**
     * 根据文件扩展名获取文件类型
     *
     * @param extension 文件扩展名
     * @return 文件类型枚举，未找到返回null
     */
    public static FileType getByExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }
        
        String lowerExt = extension.toLowerCase();
        if (lowerExt.startsWith(".")) {
            lowerExt = lowerExt.substring(1);
        }
        
        for (FileType type : values()) {
            if (type.extension.equalsIgnoreCase(lowerExt)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 验证文件是否符合要求
     *
     * @param extension 文件扩展名
     * @param fileSize 文件大小（字节）
     * @return 验证结果
     */
    public static ValidationResult validate(String extension, long fileSize) {
        FileType type = getByExtension(extension);
        
        if (type == null) {
            return ValidationResult.invalid("不支持的文件类型: " + extension);
        }
        
        if (!type.allowed) {
            return ValidationResult.invalid("文件类型不被允许上传: " + type.description);
        }
        
        if (fileSize > type.maxSize) {
            return ValidationResult.invalid(
                String.format("文件大小超出限制，最大支持%s，当前文件%s", 
                    formatFileSize(type.maxSize), formatFileSize(fileSize)));
        }
        
        return ValidationResult.valid();
    }

    /**
     * 格式化文件大小显示
     */
    private static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 验证结果类
     */
    @Getter
    @AllArgsConstructor
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}