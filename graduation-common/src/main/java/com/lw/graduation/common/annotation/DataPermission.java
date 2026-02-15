package com.lw.graduation.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * 用于标记需要进行数据权限控制的方法
 *
 * @author lw
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
    
    /**
     * 权限类型
     */
    PermissionType value() default PermissionType.DEFAULT;
    
    /**
     * 权限类型枚举
     */
    enum PermissionType {
        /**
         * 默认权限 - 只能访问自己的数据
         */
        DEFAULT,
        
        /**
         * 学生权限 - 只能访问自己的相关数据
         */
        STUDENT,
        
        /**
         * 教师权限 - 可以访问自己指导的学生数据
         */
        TEACHER,
        
        /**
         * 管理员权限 - 可以访问所有数据
         */
        ADMIN
    }
}