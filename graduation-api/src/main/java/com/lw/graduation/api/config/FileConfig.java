package com.lw.graduation.api.config;

import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 *
 * @author lw
 */
@Configuration
public class FileConfig {

    /**
     * 文件上传基础路径
     */
    public static final String UPLOAD_BASE_PATH = "/uploads";
    /**
     * 头像上传路径
     */
    public static final String AVATAR_DIR = "/avatar";
    /**
     * 文档上传路径
     */
    public static final String DOCUMENT_DIR = "/document";
}
