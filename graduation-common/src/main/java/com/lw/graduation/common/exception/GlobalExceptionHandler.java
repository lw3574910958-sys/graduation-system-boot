package com.lw.graduation.common.exception;

import com.lw.graduation.common.response.Result;
import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(e.getMessage());
    }
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleException(NotLoginException e) {
        return Result.error(e.getCode(),e.getMessage());
    }

}
