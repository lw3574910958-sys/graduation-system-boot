package com.lw.graduation.api.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


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
    @Schema(description = "用户类型")
    private String userType;
    
    /**
     * 头像 URL
     */
    @Schema(description = "头像 URL")
    private String avatar;

    /**
     * 院系 ID（学生/教师/院系管理员有值，系统管理员为 null）
     */
    @Schema(description = "院系 ID")
    private Long departmentId;

}
