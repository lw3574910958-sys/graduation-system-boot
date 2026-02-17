package com.lw.graduation.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 统一文件存储配置类
 * 配置文件上传的相关参数，支持多种存储策略
 *
 * @author lw
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * 文件存储根路径（本地存储时使用）
     */
    private String basePath = "./uploads";

    /**
     * 文件访问URL前缀
     */
    private String urlPrefix = "/files";

    /**
     * 默认允许的最大文件大小（字节）
     */
    private long maxFileSize = 50 * 1024 * 1024; // 50MB

    /**
     * 是否启用临时文件清理
     */
    private boolean enableCleanup = true;

    /**
     * 临时文件保留天数
     */
    private int tempFileRetentionDays = 7;

    /**
     * 存储类型：local, minio, oss
     */
    private String type = "local";

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 阿里云OSS配置
     */
    private OssConfig oss = new OssConfig();

    /**
     * MinIO配置类
     */
    @Data
    public static class MinioConfig {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucketName = "graduation-system";
        private String region = "us-east-1";
    }

    /**
     * 阿里云OSS配置类
     */
    @Data
    public static class OssConfig {
        private String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        private String accessKeyId;
        private String accessKeySecret;
        private String bucketName;
        private String region = "cn-hangzhou";
    }
}