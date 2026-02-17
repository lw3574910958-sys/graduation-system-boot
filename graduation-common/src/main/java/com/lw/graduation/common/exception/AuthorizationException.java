package com.lw.graduation.common.exception;

import com.lw.graduation.common.enums.ExceptionType;
import lombok.Getter;

/**
 * 权限异常
 * 用于处理权限不足或越权访问的情况
 *
 * @author lw
 */
@Getter
public class AuthorizationException extends BusinessException {

    /**
     * 权限标识
     */
    private final String permission;

    /**
     * 资源标识
     */
    private final String resource;

    public AuthorizationException(String message) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = null;
        this.resource = null;
    }

    public AuthorizationException(String permission, String message) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = permission;
        this.resource = null;
    }

    public AuthorizationException(String permission, String resource, String message) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = permission;
        this.resource = resource;
    }
}