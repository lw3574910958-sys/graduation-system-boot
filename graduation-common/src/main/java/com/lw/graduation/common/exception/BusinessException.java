package com.lw.graduation.common.exception;

import com.lw.graduation.common.enums.ResponseCode;
import lombok.Getter;

/**
 * 自定义异常
 * 
 * @author lw
 */
@Getter
public class BusinessException extends RuntimeException {

  /**
   * 状态码
   */
  private  final Integer code;

  /**
   * 默认构造：使用通用错误码（500）
   */
  public BusinessException() {
    this(ResponseCode.ERROR);
  }

  /**
   * 使用响应码构造异常
   */
  public BusinessException(ResponseCode responseCodeEnums) {
    super(responseCodeEnums.getMessage());
    this.code = responseCodeEnums.getCode();
  }

  /**
   * 自定义状态码和消息
   */
  public BusinessException(Integer code, String message) {
    super(message);
    this.code = code;
  }

  /**
   * 仅自定义消息，默认使用 ERROR 状态码（500）
   */
  public BusinessException(String message) {
    this(ResponseCode.ERROR.getCode(), message);
  }
}
