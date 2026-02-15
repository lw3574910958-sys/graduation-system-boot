package com.lw.graduation.api.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录用户信息
 *
 * @author lw
 */
@Data
@Schema(description = "用户信息")
public class LoginUserInfoVO {
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
     * 角色
     */
    @Schema(description = "角色")
    private String userType; // STUDENT, TEACHER, ADMIN

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime createdAt;
}
