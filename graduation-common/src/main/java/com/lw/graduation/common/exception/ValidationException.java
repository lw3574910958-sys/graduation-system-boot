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
    
    private final String fieldName;
    private final Object rejectedValue;

    public ValidationException(String message) {
        super(ExceptionType.VALIDATION.getCode(), message);
        this.fieldName = null;
        this.rejectedValue = null;
    }
    
    public ValidationException(String message, String fieldName) {
        super(ExceptionType.VALIDATION.getCode(), message);
        this.fieldName = fieldName;
        this.rejectedValue = null;
    }
    
    public ValidationException(String message, String fieldName, Object rejectedValue) {
        super(ExceptionType.VALIDATION.getCode(), message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }
}