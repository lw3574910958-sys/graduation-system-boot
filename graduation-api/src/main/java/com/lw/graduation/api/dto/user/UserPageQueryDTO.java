package com.lw.graduation.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页查询 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "用户分页查询参数")
public class UserPageQueryDTO {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 用户类型
     */
    @Schema(description = "用户类型 (student-学生, teacher-教师, admin-管理员)")
    private String userType; // 对应 UserType 枚举

    /**
     * 状态
     */
    @Schema(description = "状态 (1-启用, 0-禁用)")
    private Integer status;

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码必须大于0")
    @Schema(description = "当前页码", defaultValue = "1")
    private Integer current = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "每页大小", defaultValue = "10")
    private Integer size = 10;
}
