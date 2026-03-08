package com.lw.graduation.api.vo.student;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生视图对象 (View Object)
 * 用于向前端展示学生的详细信息。
 *
 * @author lw
 */
@Data
@Schema(description = "学生信息视图对象")
public class StudentVO {

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long id;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentId;

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
     * 性别 (1-男，0-女)
     */
    @Schema(description = "性别 (1-男，0-女)")
    private Integer gender;

    /**
     * 专业
     */
    @Schema(description = "专业")
    private String major;

    /**
     * 班级
     */
    @Schema(description = "班级")
    private String className;

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
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;
}
