package com.lw.graduation.api.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户视图对象 (View Object)
 * 用于向外部（如前端）展示系统用户的详细信息。
 * 对应领域层的 SysUser 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "系统用户信息视图对象")
public class UserListInfoVO {

    /**
     * 用户ID
     */
    private Long id;

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
     * 用户类型 (student-学生, teacher-教师, admin-管理员)
     */
    @Schema(description = "用户类型 (student-学生, teacher-教师, admin-管理员)")
    private String userType;

    /**
     * 状态 (1-启用, 0-禁用)
     */
    @Schema(description = "状态 (1-启用, 0-禁用)")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD) // 格式化时间输出
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime lastLoginAt;

    /**
     * 头像URL或存储路径
     */
    @Schema(description = "头像URL或存储路径")
    private String avatar;
}
