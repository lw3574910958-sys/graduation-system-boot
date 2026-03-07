package com.lw.graduation.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "修改自己密码参数")
public class UserChangePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码（明文）")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码（明文）")
    private String newPassword;
}
