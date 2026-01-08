package com.lw.graduation.api.service.auth;

import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.vo.auth.UserVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 认证服务
 *
 * @author lw
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return 登录成功后的 token
     */
    String login(LoginDTO dto);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 获取验证码图片
     *
     * @param response 响应
     * @throws IOException IO异常
     */
    String generateCaptcha(HttpServletResponse response) throws IOException;
}