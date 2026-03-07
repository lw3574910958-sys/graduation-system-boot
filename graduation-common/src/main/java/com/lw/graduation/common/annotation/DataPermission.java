package com.lw.graduation.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 用于控制用户对数据的访问权限
 *
 * @author lw
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 数据权限类型枚举
     */
    enum Type {
        /**
         * 仅限本人数据
         */
        SELF,
        
        /**
         * 教师查看指导学生数据
         */
        TEACHER_STUDENT,
        
        /**
         * 院系内数据
         */
        DEPARTMENT,
        
        /**
         * 全部数据（管理员权限）
         */
        ALL
    }
    
    /**
     * 数据权限类型
     * @return 权限类型
     */
    Type value() default Type.ALL;
    
    /**
     * 是否启用数据权限控制
     * @return true表示启用，false表示禁用
     */
    boolean enabled() default true;
}