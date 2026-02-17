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

    /**
     * 字段名称
     */
    private final String fieldName;

    /**
     * 错误值
     */
    private final Object rejectedValue;

    public ValidationException(String message) {
        super(ExceptionType.VALIDATION.getCode(), message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public ValidationException(String fieldName, String message) {
        super(ExceptionType.VALIDATION.getCode(), message);
        this.fieldName = fieldName;
        this.rejectedValue = null;
    }

    public ValidationException(String fieldName, Object rejectedValue, String message) {
        super(ExceptionType.VALIDATION.getCode(), message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }
}