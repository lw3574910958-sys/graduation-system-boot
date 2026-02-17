package com.lw.graduation.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web配置类
 * 配置静态资源映射，使上传的文件可以通过HTTP访问
 *
 * @author lw
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.dir:${UPLOAD_DIR:D:/Project/myapps/graduation-system/data/uploadFiles}}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径映射
        String fileSystemPath = Paths.get(uploadDir).toAbsolutePath().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + fileSystemPath + "/");
    }
}