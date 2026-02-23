package com.lw.graduation.common.exception;

import com.lw.graduation.common.enums.ExceptionType;
import lombok.Getter;

/**
 * 数据访问异常
 * 用于处理数据库访问或操作失败的情况
 *
 * @author lw
 */
@Getter
public class DataAccessException extends BusinessException {

    public DataAccessException(String message) {
        super(ExceptionType.DATA_ACCESS.getCode(), message);
    }
}