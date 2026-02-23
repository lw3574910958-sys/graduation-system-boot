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
     * 配置项：file.storage.base-path
     */
    private String basePath;

    /**
     * 文件访问URL前缀
     * 配置项：file.storage.url-prefix
     */
    private String urlPrefix;

    /**
     * 默认允许的最大文件大小（字节）
     * 配置项：file.storage.max-file-size
     * 对应 spring.servlet.multipart.max-file-size
     */
    private Long maxFileSize;

    /**
     * 默认允许的最大请求大小（字节）
     * 配置项：file.storage.max-request-size
     * 对应 spring.servlet.multipart.max-request-size
     */
    private Long maxRequestSize;

    /**
     * 是否启用临时文件清理
     * 配置项：file.storage.enable-cleanup
     */
    private Boolean enableCleanup;

    /**
     * 临时文件保留天数
     * 配置项：file.storage.temp-file-retention-days
     */
    private Integer tempFileRetentionDays;

    /**
     * 存储类型：local, minio, oss
     * 配置项：file.storage.type
     */
    private String type;

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 阿里云OSS配置
     */
    private OssConfig oss = new OssConfig();

    /**
     * 获取基础路径，提供默认值
     * @return 基础路径
     */
    public String getBasePath() {
        return basePath != null ? basePath : System.getenv().getOrDefault("UPLOAD_DIR", "./uploads");
    }

    /**
     * 获取URL前缀，提供默认值
     * @return URL前缀
     */
    public String getUrlPrefix() {
        return urlPrefix != null ? urlPrefix : "/files";
    }

    /**
     * 获取最大文件大小，提供默认值
     * @return 最大文件大小（字节）
     */
    public long getMaxFileSize() {
        return maxFileSize != null ? maxFileSize : 50 * 1024 * 1024; // 50MB
    }

    /**
     * 获取最大请求大小，提供默认值
     * @return 最大请求大小（字节）
     */
    public long getMaxRequestSize() {
        return maxRequestSize != null ? maxRequestSize : 100 * 1024 * 1024; // 100MB
    }

    /**
     * 获取存储类型，提供默认值
     * @return 存储类型
     */
    public String getType() {
        return type != null ? type : "local";
    }

    /**
     * 是否启用清理功能
     * @return 是否启用
     */
    public boolean isEnableCleanup() {
        return enableCleanup != null ? enableCleanup : true;
    }

    /**
     * 获取临时文件保留天数
     * @return 保留天数
     */
    public int getTempFileRetentionDays() {
        return tempFileRetentionDays != null ? tempFileRetentionDays : 7;
    }

    /**
     * MinIO配置类
     */
    @Data
    public static class MinioConfig {
        /** MinIO服务地址 */
        private String endpoint;
        /** 访问密钥 */
        private String accessKey;
        /** 秘密密钥 */
        private String secretKey;
        /** 存储桶名称 */
        private String bucketName;
        /** 区域 */
        private String region;

        /**
         * 获取MinIO端点，提供默认值
         * @return 端点URL
         */
        public String getEndpoint() {
            return endpoint != null ? endpoint : "http://localhost:9000";
        }

        /**
         * 获取访问密钥，提供默认值
         * @return 访问密钥
         */
        public String getAccessKey() {
            return accessKey != null ? accessKey : "minioadmin";
        }

        /**
         * 获取秘密密钥，提供默认值
         * @return 秘密密钥
         */
        public String getSecretKey() {
            return secretKey != null ? secretKey : "minioadmin";
        }

        /**
         * 获取存储桶名称，提供默认值
         * @return 存储桶名称
         */
        public String getBucketName() {
            return bucketName != null ? bucketName : "graduation-system";
        }

        /**
         * 获取区域，提供默认值
         * @return 区域
         */
        public String getRegion() {
            return region != null ? region : "us-east-1";
        }
    }

    /**
     * 阿里云OSS配置类
     */
    @Data
    public static class OssConfig {
        /** OSS服务地址 */
        private String endpoint;
        /** 访问密钥ID */
        private String accessKeyId;
        /** 访问密钥Secret */
        private String accessKeySecret;
        /** 存储桶名称 */
        private String bucketName;
        /** 区域 */
        private String region;

        /**
         * 获取OSS端点，提供默认值
         * @return 端点URL
         */
        public String getEndpoint() {
            return endpoint != null ? endpoint : "oss-cn-hangzhou.aliyuncs.com";
        }

        /**
         * 获取区域，提供默认值
         * @return 区域
         */
        public String getRegion() {
            return region != null ? region : "cn-hangzhou";
        }
    }

    /**
     * 获取人类可读的文件大小格式
     * @param bytes 字节数
     * @return 格式化字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 获取配置摘要信息
     * @return 配置信息字符串
     */
    public String getConfigSummary() {
        return String.format("FileStorageConfig{basePath='%s', urlPrefix='%s', maxFileSize=%s, type='%s', enableCleanup=%s}",
                getBasePath(), getUrlPrefix(), formatFileSize(getMaxFileSize()), getType(), isEnableCleanup());
    }
}