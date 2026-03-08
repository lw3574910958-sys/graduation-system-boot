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
    @Schema(description = "状态 (1-启用，0-禁用)", defaultValue = "1")
    private Integer status = 1; // 默认启用
    
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
     * 所属院系 ID（学生/教师/管理员必填）
     */
    @Schema(description = "所属院系 ID")
    private Long departmentId;
    
    /**
     * 头像 URL 或存储路径
     */
    @Schema(description = "头像 URL 或存储路径")
    private String avatar;
    
    /**
     * 班级（仅学生需要）
     */
    @Schema(description = "班级（仅学生需要）")
    private String className;
    
    /**
     * 职称（仅教师需要）
     */
    @Schema(description = "职称（仅教师需要）")
    private String title;
    
    /**
     * 学号（仅学生需要）
     */
    @Schema(description = "学号（仅学生需要）")
    private String studentId;
    
    /**
     * 工号（仅教师需要）
     */
    @Schema(description = "工号（仅教师需要）")
    private String teacherId;
    
    /**
     * 管理员编号（仅管理员需要）
     */
    @Schema(description = "管理员编号（仅管理员需要）")
    private String adminId;
    
    /**
     * 性别（学生/教师需要）：0-女，1-男
     */
    @Schema(description = "性别（学生/教师需要）：0-女，1-男")
    private Integer gender;
    
    /**
     * 专业（仅学生需要）
     */
    @Schema(description = "专业（仅学生需要）")
    private String major;
}
