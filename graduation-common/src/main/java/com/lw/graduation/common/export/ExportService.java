package com.lw.graduation.common.export;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 数据导出服务接口
 *
 * @param <T> 导出数据类型
 * @author liwei
 * @since 1.0.0
 */
public interface ExportService<T> {

    /**
     * 导出Excel文件
     *
     * @param data 数据列表
     * @param fileName 文件名
     * @param sheetName 工作表名称
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportToExcel(List<T> data, String fileName, String sheetName, HttpServletResponse response) throws IOException;

    /**
     * 导出CSV文件
     *
     * @param data 数据列表
     * @param fileName 文件名
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportToCsv(List<T> data, String fileName, HttpServletResponse response) throws IOException;

    /**
     * 获取导出字段映射
     *
     * @return 字段映射关系
     */
    List<ExportField> getExportFields();

    /**
     * 导出字段定义
     */
    class ExportField {
        private String fieldName;
        private String displayName;
        private int width;
        private String format;

        public ExportField(String fieldName, String displayName) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.width = 20;
        }

        public ExportField(String fieldName, String displayName, int width) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.width = width;
        }

        public ExportField(String fieldName, String displayName, int width, String format) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.width = width;
            this.format = format;
        }

        // getter和setter方法
        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }
}