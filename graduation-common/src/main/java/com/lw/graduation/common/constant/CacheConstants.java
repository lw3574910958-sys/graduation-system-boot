package com.lw.graduation.common.constant;

/**
 * 缓存常量类
 * 定义缓存相关的常量和键名规范
 *
 * @author lw
 */
public class CacheConstants {

    /**
     * 缓存键前缀
     */
    public static class KeyPrefix {
        /** 用户信息缓存前缀 */
        public static final String USER_INFO = "user:info:";
        /** 院系信息缓存前缀 */
        public static final String DEPARTMENT_INFO = "department:info:";
        /** 课题信息缓存前缀 */
        public static final String TOPIC_INFO = "topic:info:";
        /** 选题信息缓存前缀 */
        public static final String SELECTION_INFO = "selection:info:";
        /** 成绩信息缓存前缀 */
        public static final String GRADE_INFO = "grade:info:";
        /** 通知信息缓存前缀 */
        public static final String NOTICE_INFO = "notice:info:";
        
        /** 当前用户信息缓存前缀 */
        public static final String CURRENT_USER = "user:current:";
        /** 文档信息缓存前缀 */
        public static final String DOCUMENT_INFO = "document:info:";
        /** 所有院系列表缓存键 */
        public static final String ALL_DEPARTMENTS = "departments:all";
    }

    /**
     * 缓存特殊值
     */
    public static class CacheValue {
        /** 空值标记，用于防止缓存穿透 */
        public static final String NULL_MARKER = "NULL";
        /** 空值缓存过期时间（秒）- 较短时间 */
        public static final int NULL_EXPIRE = 120; // 2分钟
    }

    /**
     * 缓存过期时间（秒）- 实际使用的常量
     */
    public static class ExpireTime {
        // 实际使用的过期时间
        /** 院系信息缓存过期时间：2小时 */
        public static final int DEPARTMENT_INFO_EXPIRE = 7200;
        /** 所有院系列表缓存过期时间：2小时 */
        public static final int ALL_DEPARTMENTS_EXPIRE = 7200;
        /** 温数据缓存过期时间：1小时 */
        public static final int WARM_DATA_EXPIRE = 3600;
        /** 冷数据缓存过期时间：30分钟 */
        public static final int COLD_DATA_EXPIRE = 1800;
        /** 用户信息缓存过期时间：15分钟 */
        public static final int USER_INFO_EXPIRE = 900;
        /** 当前用户信息缓存过期时间：15分钟 */
        public static final int CURRENT_USER_EXPIRE = 900;
    }

}