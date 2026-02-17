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

    /**
     * 数据库操作类型
     */
    private final String operation;

    /**
     * 表名或实体名
     */
    private final String tableOrEntity;

    public DataAccessException(String message) {
        super(ExceptionType.DATA_ACCESS.getCode(), message);
        this.operation = null;
        this.tableOrEntity = null;
    }

    public DataAccessException(String operation, String message) {
        super(ExceptionType.DATA_ACCESS.getCode(), message);
        this.operation = operation;
        this.tableOrEntity = null;
    }

    public DataAccessException(String operation, String tableOrEntity, String message) {
        super(ExceptionType.DATA_ACCESS.getCode(), message);
        this.operation = operation;
        this.tableOrEntity = tableOrEntity;
    }
}