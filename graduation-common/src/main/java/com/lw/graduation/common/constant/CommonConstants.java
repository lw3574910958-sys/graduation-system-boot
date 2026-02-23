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
}