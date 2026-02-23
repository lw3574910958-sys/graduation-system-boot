package com.lw.graduation.common.response;

import com.lw.graduation.common.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 统一响应结果
 *
 * @author lw
 * @param <T> 响应数据类型
 */
@Getter
@ToString
@AllArgsConstructor
public class Result<T> {

    /**
     * 状态码（使用基本类型 int，避免 NPE 和装箱开销）
     */
    private final int code;

    /**
     * 提示信息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final T data;

    // ---------------- 构造函数（私有，通过静态工厂方法创建） ----------------
    private Result(int code) {
        this(code, null, null);
    }

    private Result(int code, String message) {
        this(code, message, null);
    }

    // ---------------- 成功响应 ----------------

    /**
     * 成功返回结果（无数据，使用默认成功码和消息）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResponseCode.SUCCESS.getCode());
    }

    /**
     * 成功返回结果（自定义消息，无数据）
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功返回结果（带数据，使用默认成功消息）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果（带数据和自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    // ---------------- 失败响应 ----------------

    /**
     * 失败返回结果（使用默认错误码 ERROR 和其消息）
     */
    public static <T> Result<T> error() {
        return error(ResponseCode.ERROR);
    }

    /**
     * 失败返回结果（自定义错误消息，使用默认错误码 500）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResponseCode.ERROR.getCode(), message);
    }

    /**
     * 失败返回结果（指定错误码和消息）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败返回结果（使用预定义的 ResponseCode 枚举）
     */
    public static <T> Result<T> error(ResponseCode responseCode) {
        return new Result<>(responseCode.getCode(), responseCode.getMessage());
    }
}