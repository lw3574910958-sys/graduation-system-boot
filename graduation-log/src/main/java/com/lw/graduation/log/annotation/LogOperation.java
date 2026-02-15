package com.lw.graduation.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 用于标记需要自动记录操作日志的方法
 * 基于项目AOP基础设施实现无侵入式日志记录
 *
 * @author lw
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogOperation {
    
    /**
     * 操作描述
     * 例如："创建用户"、"删除课题"、"审核文档"
     */
    String value();
    
    /**
     * 模块名称（可选）
     * 用于日志分类统计
     * 例如："用户管理"、"课题管理"、"文档管理"
     */
    String module() default "";
    
    /**
     * 是否记录请求参数（默认false）
     * 注意：为避免记录敏感信息，谨慎启用此选项
     */
    boolean recordParams() default false;
    
    /**
     * 是否忽略返回结果记录（默认false）
     * 用于避免记录大量返回数据
     */
    boolean ignoreResult() default false;
}