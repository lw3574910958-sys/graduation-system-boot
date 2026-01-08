package com.lw.graduation.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置
 * 用于 Knife4j 文档生成
 *
 * @author lw
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("高校毕业设计管理系统 API")
                        .version("1.0.0")
                        .description("基于 Spring Boot 3 + Vue 3 的毕业设计全流程管理")
                        .contact(new Contact()
                                .name("lw")
                                .email("3574910958@qq.com")
                                .url("https://github.com/lw3574910958-sys"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}