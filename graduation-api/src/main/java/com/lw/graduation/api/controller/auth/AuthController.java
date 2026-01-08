package com.lw.graduation.api.controller.auth;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.api.vo.auth.UserVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public Result<String> login(@Validated @RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        return Result.success(token);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getCurrentUser() {
        // 从 Sa-Token 中获取当前登录用户ID
        Long userId = Long.valueOf(String.valueOf(StpUtil.getLoginId()));
        UserVO userVO = authService.getCurrentUser(userId);
        return Result.success(userVO);
    }

    /**
     * 获取验证码
     *
     * @param response 响应
     * @return 验证码唯一标识
     */
    @GetMapping("/captcha")
    @SaIgnore // 忽略验证码接口鉴权
    @Operation(summary = "获取验证码")
    public Result<String> generateCaptcha(HttpServletResponse response) throws Exception {
        String captchaKey = authService.generateCaptcha(response);
        return Result.success(captchaKey);
    }
}
