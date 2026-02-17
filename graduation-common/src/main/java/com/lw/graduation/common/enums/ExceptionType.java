package com.lw.graduation.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常类型枚举
 * 用于分类和标识不同类型的异常
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum ExceptionType {

    /**
     * 业务异常
     */
    BUSINESS(1000, "业务异常"),

    /**
     * 参数验证异常
     */
    VALIDATION(2000, "参数验证异常"),

    /**
     * 权限异常
     */
    AUTHORIZATION(3000, "权限异常"),

    /**
     * 认证异常
     */
    AUTHENTICATION(4000, "认证异常"),

    /**
     * 数据访问异常
     */
    DATA_ACCESS(5000, "数据访问异常"),

    /**
     * 文件操作异常
     */
    FILE_OPERATION(6000, "文件操作异常"),

    /**
     * 系统异常
     */
    SYSTEM(9000, "系统异常");

    /**
     * 异常类型编码
     */
    private final Integer code;

    /**
     * 异常类型描述
     */
    private final String description;

    /**
     * 根据编码获取异常类型
     *
     * @param code 编码
     * @return 异常类型，未找到返回null
     */
    public static ExceptionType getByCode(Integer code) {
        for (ExceptionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}