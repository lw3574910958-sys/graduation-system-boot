package com.lw.graduation.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码校验结果视图对象
 *
 * @author lw
 */
@Data
@Schema(description = "验证码校验结果")
public class CaptchaCheckVO {
    @Schema(description = "验证码是否有效", example = "true")
    private Boolean valid;

    public CaptchaCheckVO(Boolean valid) {
        this.valid = valid;
    }
}
