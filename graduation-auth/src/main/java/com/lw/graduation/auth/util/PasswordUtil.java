package com.lw.graduation.auth.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 * 用于密码加密和校验
 * 注意：由于使用了 @Component 和 @Autowired，此工具类需要在 Spring 上下文中被管理。
 * 如果需要在非 Spring 管理的类中使用，可能需要其他方式获取 PasswordEncoder 实例。
 */
@Component
@RequiredArgsConstructor
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder;

    /**
     * 加密原始密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证输入的原始密码与数据库中存储的已加密密码是否匹配，兼容旧格式密码。
     * 如果存储的密码不是BCrypt格式（即没有$2a$、$2b$或$2y$前缀），则直接比较明文。
     *
     * @param rawPassword     用户输入的原始明文密码。
     * @param encodedPassword 数据库中存储的已加密密码（哈希值）。
     * @return 如果输入的密码与存储的密码匹配，则返回 true；否则返回 false。
     *         如果任一参数为 null，也返回 false。
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        // 检查是否为BCrypt格式
        if (isEncodedWithBCrypt(encodedPassword)) {
            // BCrypt格式，使用PasswordEncoder验证
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } else {
            // 非BCrypt格式，可能是旧的明文密码，直接比较
            return rawPassword.equals(encodedPassword);
        }
    }

    /**
     * 检查密码是否为BCrypt格式
     * BCrypt格式通常以$2a$、$2b$或$2y$开头
     * @param encodedPassword 编码后的密码
     * @return 是否为BCrypt格式
     */
    private boolean isEncodedWithBCrypt(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() < 4) {
            return false;
        }
        return encodedPassword.startsWith("$2a$") ||
               encodedPassword.startsWith("$2b$") ||
               encodedPassword.startsWith("$2y$");
    }

}