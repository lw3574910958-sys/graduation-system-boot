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

    public AuthorizationException(String message) {
        super(ExceptionType.AUTHORIZATION.getCode(), message);
    }
}