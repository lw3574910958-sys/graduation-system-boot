package com.lw.graduation.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 安全配置类
 *
 * @author lw
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置 PasswordEncoder Bean。
     * 使用 DelegatingPasswordEncoder 作为主要的密码编码器，
     * 它可以支持多种编码算法，并能自动识别和处理不同格式的密码。
     * 默认使用 BCryptPasswordEncoder 对新密码进行编码。
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder delegatingPasswordEncoder =
                (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // Set a default encoder (e.g., bcrypt)
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return delegatingPasswordEncoder;
    }
}