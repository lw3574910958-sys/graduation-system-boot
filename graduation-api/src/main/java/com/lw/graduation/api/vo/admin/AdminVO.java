package com.lw.graduation.api.vo.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 管理员信息视图对象
 * </p>
 *
 * @author lw
 * @since 2026-03-08
 */
@Data
@Schema(description = "管理员信息视图对象")
public class AdminVO {

    @Schema(description = "管理员 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "管理员编号")
    private String adminId;

    @Schema(description = "所属院系 ID(系统管理员为 NULL)")
    private Long departmentId;

    @Schema(description = "所属院系名称 (系统管理员为 NULL)")
    private String departmentName;

    @Schema(description = "角色级别 (0-系统管理员，1-院系管理员)")
    private Integer roleLevel;

    @Schema(description = "角色级别描述")
    private String roleLevelDesc;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
