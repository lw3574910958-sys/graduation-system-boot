package com.lw.graduation.api.service.auth;

import com.lw.graduation.api.vo.auth.CaptchaVO;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.vo.user.LoginUserInfoVO;


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
     * 获取验证码DTO（新方法，返回JSON格式）
     *
     * @return CaptchaDTO 包含验证码图片base64编码和唯一标识
     */
    CaptchaVO generateCaptchaDto();

    /**
     * 用户登出
     */
    void logout();

    /**
     * 检查验证码
     *
     * @param captchaKey 验证码键
     * @param captchaCode 验证码
     * @return 验证结果
     */
    boolean checkCaptcha(String captchaKey, String captchaCode);

    /**
     * 刷新token，延长token有效期
     *
     * @return 新的token
     */
    String refreshToken();

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    LoginUserInfoVO getCurrentUser();
}