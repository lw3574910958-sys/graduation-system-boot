package com.lw.graduation.test.config;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Web MVC测试配置注解
 * 专为控制器测试设计，包含事务管理器配置
 *
 * @author lw
 */
@WebMvcTest
@ActiveProfiles("test")
@Import(TestTransactionConfig.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebMvcTestConfig {
}