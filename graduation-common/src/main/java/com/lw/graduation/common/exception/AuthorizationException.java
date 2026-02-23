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
    
    private final String permission;
    private final String resource;
    private final String operation;

    public AuthorizationException(String message) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = null;
        this.resource = null;
        this.operation = null;
    }
    
    public AuthorizationException(String message, String permission) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = permission;
        this.resource = null;
        this.operation = null;
    }
    
    public AuthorizationException(String message, String permission, String resource) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = permission;
        this.resource = resource;
        this.operation = null;
    }
    
    public AuthorizationException(String message, String permission, String resource, String operation) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
        this.permission = permission;
        this.resource = resource;
        this.operation = operation;
    }
}