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

    /*@ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, "参数校验失败: " + msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getFieldError().getDefaultMessage();
        return Result.error(400, "参数绑定失败: " + msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().iterator().next().getMessage();
        return Result.error(400, "参数校验失败: " + msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error(400, "请求体格式错误");
    }*/
}
