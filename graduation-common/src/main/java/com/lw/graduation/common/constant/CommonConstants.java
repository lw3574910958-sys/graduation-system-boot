package com.lw.graduation.common.constant;

/**
 * 通用常量类
 * 集中管理项目中的各种常量
 *
 * @author lw
 */
public class CommonConstants {

    /**
     * 时间格式常量
     */
    public static class DateTimeFormat {
        /** 标准日期时间格式 */
        public static final String STANDARD = "yyyy-MM-dd HH:mm:ss";
        /** 日期格式 */
        public static final String DATE_ONLY = "yyyy-MM-dd";
        /** 时间格式 */
        public static final String TIME_ONLY = "HH:mm:ss";
        /** 紧凑日期时间格式 */
        public static final String COMPACT = "yyyyMMddHHmmss";
    }

    /**
     * 数字常量
     */
    public static class Numbers {
        /** 默认页码 */
        public static final int DEFAULT_PAGE = 1;
        /** 默认页面大小 */
        public static final int DEFAULT_SIZE = 10;
        /** 最大页面大小 */
        public static final int MAX_SIZE = 100;
        /** 最小页面大小 */
        public static final int MIN_SIZE = 1;
    }

    /**
     * 字符串常量
     */
    public static class Strings {
        /** 空字符串 */
        public static final String EMPTY = "";
        /** 空格 */
        public static final String SPACE = " ";
        /** 逗号 */
        public static final String COMMA = ",";
        /** 下划线 */
        public static final String UNDERLINE = "_";
        /** 中横线 */
        public static final String HYPHEN = "-";
    }

    /**
     * 系统常量
     */
    public static class System {
        /** 系统名称 */
        public static final String SYSTEM_NAME = "graduation-system";
        /** 版本号 */
        public static final String VERSION = "1.0.0";
        /** 开发者 */
        public static final String DEVELOPER = "lw";
    }
}