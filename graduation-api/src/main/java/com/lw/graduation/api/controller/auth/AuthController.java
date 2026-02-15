package com.lw.graduation.api.controller.auth;

import cn.dev33.satoken.annotation.SaIgnore;
import com.lw.graduation.api.vo.auth.CaptchaVO;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.api.vo.auth.CaptchaCheckVO;
import com.lw.graduation.api.vo.auth.LoginVO;
import com.lw.graduation.api.vo.user.LoginUserInfoVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 认证服务控制器
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证管理相关接口")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    /**
     * 登录
     *
     * @param dto 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    @SaIgnore // 忽略登录鉴权
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        return Result.success(new LoginVO(token));
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取验证码
     *
     * @return 验证码信息DTO
     */
    @GetMapping("/captcha/get")
    @SaIgnore // 忽略验证码接口鉴权
    @Operation(summary = "获取验证码")
    public Result<CaptchaVO> generateCaptcha() {
        CaptchaVO captcha = authService.generateCaptchaDto();
        return Result.success(captcha);
    }

    /**
     * 检查验证码
     *
     * @param captchaKey 验证码键
     * @param captchaCode 验证码
     * @return 验证结果
     */
    @GetMapping("/captcha/check")
    @SaIgnore // 忽略验证码检查接口鉴权
    @Operation(summary = "检查验证码")
    public Result<CaptchaCheckVO> checkCaptcha(
            @RequestParam("captchaKey") String captchaKey,
            @RequestParam("captchaCode") String captchaCode) {
        boolean isValid = authService.checkCaptcha(captchaKey, captchaCode);
        return Result.success(new CaptchaCheckVO(isValid));
    }

    /**
     * 刷新token，保持token有效期
     *
     * @return 新的token
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新token")
    public Result<LoginVO> refreshToken() {
        String refreshToken = authService.refreshToken();
        return Result.success(new LoginVO(refreshToken));
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<LoginUserInfoVO> getCurrentUser() {
        LoginUserInfoVO userVO = authService.getCurrentUser();
        return Result.success(userVO);
    }
}
