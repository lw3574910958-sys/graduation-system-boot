package com.lw.graduation.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 配置属性类
 * 完全从 application.yml 的 sa-token 配置项读取，不设字段初始值
 * 默认值通过 getter 方法提供，风格与 FileStorageProperties 一致
 *
 * @author lw
 */
@Data
@Component
@ConfigurationProperties(prefix = "sa-token")
public class SaTokenProperties {

    /** Token 名称 */
    private String tokenName;

    /** Token 前缀（如 Bearer）*/
    private String tokenPrefix;

    /** 是否从 Header 中读取 Token */
    private Boolean isReadHeader;

    /** 是否从 Cookie 中读取 Token */
    private Boolean isReadCookie;

    /** Token 有效期（秒）*/
    private Integer timeout;

    /** 活跃超时时间（秒）*/
    private Integer activeTimeout;

    /** 是否允许多端并发登录 */
    private Boolean isConcurrent;

    /** 是否共用同一 Token */
    private Boolean isShare;

    /** Token 生成风格 */
    private String tokenStyle;

    /** 是否输出操作日志 */
    private Boolean isLog;

    // ========== Getter with fallback (like FileStorageProperties) ==========

    public String getTokenName() {
        return tokenName != null ? tokenName : "user_token";
    }

    public String getTokenPrefix() {
        return tokenPrefix != null ? tokenPrefix : "";
    }

    public boolean isReadHeader() {
        return isReadHeader != null && isReadHeader;
    }

    public boolean isReadCookie() {
        return isReadCookie != null && isReadCookie;
    }

    public int getTimeout() {
        return timeout != null ? timeout : 2592000; // 30天
    }

    public int getActiveTimeout() {
        return activeTimeout != null ? activeTimeout : 1800; // 30分钟
    }

    public boolean isConcurrent() {
        return isConcurrent != null && isConcurrent;
    }

    public boolean isShare() {
        return isShare != null && isShare;
    }

    public String getTokenStyle() {
        return tokenStyle != null ? tokenStyle : "uuid";
    }

    public boolean isLog() {
        return isLog != null && isLog;
    }

    /**
     * 获取配置摘要信息
     * @return 配置摘要字符串
     */
    public String getConfigSummary() {
        return String.format(
                "SaTokenConfig{tokenName='%s', timeout=%ds, activeTimeout=%ds, isConcurrent=%s, tokenStyle='%s'}",
                getTokenName(),
                getTimeout(),
                getActiveTimeout(),
                isConcurrent(),
                getTokenStyle()
        );
    }
}