package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 * 定义文档审核的各种状态及转换规则
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum ReviewStatus {

    /**
     * 待审核
     */
    PENDING(0, "待审"),
    
    /**
     * 审核通过
     */
    APPROVED(1, "通过"),
    
    /**
     * 审核驳回
     */
    REJECTED(2, "驳回");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取审核状态枚举
     *
     * @param value 审核状态值
     * @return 对应的枚举，未找到返回null
     */
    public static ReviewStatus getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        
        for (ReviewStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     *
     * @param value 审核状态值
     * @return 有效返回true
     */
    public static boolean isValid(Integer value) {
        return getByValue(value) != null;
    }

    /**
     * 判断是否为最终状态（通过或驳回）
     *
     * @return 最终状态返回true
     */
    public boolean isFinalStatus() {
        return this == APPROVED || this == REJECTED;
    }

    /**
     * 判断是否可以重新提交审核
     *
     * @return 可以重新提交返回true
     */
    public boolean canResubmit() {
        return this == REJECTED;
    }
}
