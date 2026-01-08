package com.lw.graduation.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录参数
 *
 * @author lw
 */
@Data
@Schema(description = "登录参数")
public class LoginDTO {
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String captchaCode;

    /**
     * 验证码唯一标识
     */
    @NotBlank(message = "验证码唯一标识不能为空")
    @Schema(description = "验证码唯一标识")
    private String captchaKey;
}