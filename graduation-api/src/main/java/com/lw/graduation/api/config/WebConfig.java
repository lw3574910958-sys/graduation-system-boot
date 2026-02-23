package com.lw.graduation.api.config;

import com.lw.graduation.common.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径映射
        String fileSystemPath = Paths.get(fileStorageProperties.getBasePath()).toAbsolutePath().toString();
        registry.addResourceHandler(fileStorageProperties.getUrlPrefix() + "/**")
                .addResourceLocations("file:" + fileSystemPath + "/");
    }
}