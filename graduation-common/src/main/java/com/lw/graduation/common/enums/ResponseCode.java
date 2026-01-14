package com.lw.graduation.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@AllArgsConstructor
@Getter
public enum ResponseCode {

    // --- 通用成功/失败 ---
    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功！"),
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "404 错误，请检查路径是否正确"),

    /**
     * 操作失败
     */
    ERROR(500, "操作失败！"),

    // --- 参数校验相关 ---
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "请求参数错误"),

    // --- 认证授权相关 ---
    /**
     * 未登录
     */
    UNAUTHORIZED(401, "未登录或登录已过期，请重新登录"),
    /**
     * 权限不足
     */
    FORBIDDEN(403, "权限不足，无法访问"),

    // --- 验证码相关 ---
    /**
     * 创建验证码失败
     */
    CREATE_CAPTCHA_ERROR(1101, "获取验证码失败！"),
    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(1102, "验证码错误！"),
    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(1103, "验证码已过期！"),
    /**
     * 验证码不能为空
     */
    CAPTCHA_REQUIRED(1104, "请输入验证码"),

    // --- 用户相关 ---
    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(1001, "用户名已存在！"),
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1002, "用户不存在！"),
    /**
     * 密码错误
     */
    PASSWORD_ERROR(1003, "密码错误！"),
    /**
     * 用户名或密码错误 (通常用于登录失败)
     */
    USERNAME_PASSWORD_ERROR(1004, "用户名或密码错误！"),
    /**
     * 账户被禁用
     */
    ACCOUNT_DISABLED(1005, "账户已被禁用！"),
    /**
     * 账户被锁定
     */
    ACCOUNT_LOCKED(1006, "账户已被锁定！"),
    /**
     * 密码和确认密码不一致
     */
    PASSWORD_MISMATCH(1006, "密码和确认密码不一致！"),
    /**
     * 用户类型无效
     */
    USER_TYPE_INVALID(1007, "用户类型无效！");


    /**
     * 响应码
     */
    private final int code;

    /**
     * 响应信息
     */
    private final String message;

    /**
     * 根据code获取响应码枚举
     */
    public static ResponseCode fromCode(int code) {
        for (ResponseCode responseCode : ResponseCode.values()) {
            if (responseCode.getCode() == code) {
                return responseCode;
            }
        }
        // 如果没有找到匹配的code，默认返回FAIL
        return ERROR;
    }
}
