package com.lw.graduation.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新用户 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "更新用户参数")
public class UserUpdateDTO {

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 用户类型
     */
    @Schema(description = "用户类型 (student-学生, teacher-教师, admin-管理员)")
    private String userType;

    /**
     * 状态
     */
    @Schema(description = "状态 (1-启用, 0-禁用)")
    private Integer status;

    /**
     * 头像URL或存储路径
     */
    @Schema(description = "头像URL或存储路径")
    private String avatar;
}
