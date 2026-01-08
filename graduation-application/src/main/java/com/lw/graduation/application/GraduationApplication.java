package com.lw.graduation.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 全局启动类
 *
 * @author lw
 */
@SpringBootApplication(scanBasePackages = "com.lw.graduation")
public class GraduationApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraduationApplication.class, args);
    }
}