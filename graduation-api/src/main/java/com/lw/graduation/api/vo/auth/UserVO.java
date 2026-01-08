package com.lw.graduation.api.vo.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息
 *
 * @author lw
 */
@Data
@Schema(description = "用户信息")
public class UserVO {
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
    private String role; // STUDENT, TEACHER, ADMIN

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
