package com.lw.graduation.api.dto.user;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询 DTO
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户分页查询参数")
public class UserPageQueryDTO extends BasePageQueryDTO {

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
    @Schema(description = "用户类型 (student-学生, teacher-教师, system_admin-系统管理员, department_admin-院系管理员)")
    private String userType; // 对应 UserType 枚举

    /**
     * 状态
     */
    @Schema(description = "状态 (1-启用, 0-禁用)")
    private Integer status;

    /**
     * 院系 ID（用于系统管理员按院系筛选用户）
     */
    @Schema(description = "院系 ID")
    private Long departmentId;
}
