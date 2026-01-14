package com.lw.graduation.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 创建用户 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "创建用户参数")
public class UserCreateDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须是4-20位字母、数字或下划线")
    @Schema(description = "用户名")
    private String username;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 用户类型
     */
    @NotNull(message = "用户类型不能为空")
    @Schema(description = "用户类型 (student-学生, teacher-教师, admin-管理员)")
    private String userType;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$", message = "密码必须至少包含一个字母和一个数字，长度至少为6位")
    @Schema(description = "密码")
    private String password;

    /**
     * 状态
     */
    @Schema(description = "状态 (1-启用, 0-禁用)", defaultValue = "1")
    private Integer status = 1; // 默认启用

    /**
     * 头像URL或存储路径
     */
    @Schema(description = "头像URL或存储路径")
    private String avatar;
}
