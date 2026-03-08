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
     * 头像 URL 或存储路径
     */
    @Schema(description = "头像 URL 或存储路径")
    private String avatar;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 所属院系 ID
     */
    @Schema(description = "所属院系 ID")
    private Long departmentId;

    /**
     * 所属院系名称
     */
    @Schema(description = "所属院系名称")
    private String departmentName;

    /**
     * 学号（仅学生）
     */
    @Schema(description = "学号（仅学生）")
    private String studentId;

    /**
     * 工号（仅教师）
     */
    @Schema(description = "工号（仅教师）")
    private String teacherId;

    /**
     * 管理员编号（仅管理员）
     */
    @Schema(description = "管理员编号（仅管理员）")
    private String adminId;

    /**
     * 性别（学生/教师）：0-女，1-男
     */
    @Schema(description = "性别（学生/教师）：0-女，1-男")
    private Integer gender;

    /**
     * 专业（仅学生）
     */
    @Schema(description = "专业（仅学生）")
    private String major;

    /**
     * 班级（仅学生）
     */
    @Schema(description = "班级（仅学生）")
    private String className;

    /**
     * 职称（仅教师）
     */
    @Schema(description = "职称（仅教师）")
    private String title;

    /**
     * 角色级别（仅管理员）：0-系统管理员，1-院系管理员
     */
    @Schema(description = "角色级别（仅管理员）：0-系统管理员，1-院系管理员")
    private Integer roleLevel;
}
