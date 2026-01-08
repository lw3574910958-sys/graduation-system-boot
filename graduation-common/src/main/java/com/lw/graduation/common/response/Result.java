package com.lw.graduation.common.response;

import com.lw.graduation.common.enums.ResponseCode;
import lombok.Getter;

/**
 * 统一响应结果
 *
 * @author lw
 */
@Getter
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 构造函数
     * @param code 状态码
     */
    private Result(Integer  code) {
        this.code = code;
    }

    /**
     * 构造函数
     * @param code 状态码
     * @param message 提示信息
     */
    private Result(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数
     * @param code 状态码
     * @param message 提示信息
     * @param data 数据
     */
    private Result(Integer code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果（无数据，使用默认成功消息）
     */
    public static <T> Result<T> success() {
        return success(ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 成功返回结果（自定义成功消息，无数据）
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功返回结果（带数据，使用默认成功消息）
     */
    public static <T> Result<T> success(T data) {
        return success(ResponseCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果（带数据和自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果（使用默认错误码和消息）
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
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败返回结果（使用预定义的 ResponseCode）
     */
    public static <T> Result<T> error(ResponseCode responseCodeEnums) {
        return new Result<>(responseCodeEnums.getCode(), responseCodeEnums.getMessage());
    }

}
