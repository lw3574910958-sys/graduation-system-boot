package com.lw.graduation.auth.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 */
@Configuration
public class KaptchaConfig {
    @Bean
    public DefaultKaptcha getDefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        // 边框
        properties.setProperty("kaptcha.border", "no");
        // 边框颜色
        properties.setProperty("kaptcha.border.color", "34,114,200");
        // 字体
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        // 验证码宽度
        properties.setProperty("kaptcha.image.width", "125");
        // 验证码高度
        properties.setProperty("kaptcha.image.height", "40");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 字符
        properties.setProperty("kaptcha.textproducer.char.string", "123456789");
        // 字体
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Arial,Narrow,Serif,Helvetica,Tahoma,Times New Roman,Verdana");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "38");
        // 背景颜色
        properties.setProperty("kaptcha.background.clear.from","white");
        properties.setProperty("kaptcha.background.clear.to","white");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);

        return defaultKaptcha;
    }
}

