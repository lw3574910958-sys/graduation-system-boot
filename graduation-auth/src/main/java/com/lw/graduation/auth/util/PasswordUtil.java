package com.lw.graduation.auth.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具类
 * 用于密码加密和校验
 * 注意：由于使用了 @Component 和 @Autowired，此工具类需要在 Spring 上下文中被管理。
 * 如果需要在非 Spring 管理的类中使用，可能需要其他方式获取 PasswordEncoder 实例。
 */
@Component
public class PasswordUtil {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 加密原始密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 校验原始密码与加密后的密码是否匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 匹配返回 true，否则返回 false
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}