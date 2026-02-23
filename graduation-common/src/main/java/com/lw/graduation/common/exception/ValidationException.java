package com.lw.graduation.common.exception;

import com.lw.graduation.common.enums.ExceptionType;
import lombok.Getter;

/**
 * 参数验证异常
 * 用于处理请求参数验证失败的情况
 *
 * @author lw
 */
@Getter
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ExceptionType.VALIDATION.getCode(), message);
    }
}