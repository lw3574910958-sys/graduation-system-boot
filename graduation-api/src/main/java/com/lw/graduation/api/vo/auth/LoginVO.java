package com.lw.graduation.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功返回视图对象
 *
 * @author lw
 */
@Data
@AllArgsConstructor
@Schema(description = "登录成功返回信息")
public class LoginVO {

    @Schema(description = "访问令牌（Sa-Token Token）")
    private String token;

}
