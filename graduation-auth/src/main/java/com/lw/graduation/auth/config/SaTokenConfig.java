package com.lw.graduation.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * @author lw
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器，打开注解式鉴权功能
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 放行 OPTIONS 请求（CORS 预检）
            String method = SaHolder.getRequest().getMethod();
            if ("OPTIONS".equals(method)) {
                return;
            }

            // 拦截所有请求，排除 /api/auth/**
            SaRouter.match("/api/**")
                    .notMatch("/api/auth/login")
                    .notMatch("/api/auth/captcha/get")
                    .notMatch("/api/auth/captcha/check")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/api/**");
    }
}
