package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 选题状态枚举
 * 定义学生选题申请的完整状态流转
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum SelectionStatus {

    /**
     * 待审核 - 学生提交选题申请
     */
    PENDING_REVIEW(0, "待审核"),
    
    /**
     * 审核通过 - 教师同意学生选题
     */
    APPROVED(1, "审核通过"),
    
    /**
     * 审核驳回 - 教师拒绝学生选题
     */
    REJECTED(2, "审核驳回"),
    
    /**
     * 已确认 - 学生确认选题
     */
    CONFIRMED(3, "已确认");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String description;

    /**
     * 根据值获取选题状态枚举
     *
     * @param value 状态值
     * @return 对应的枚举，未找到返回null
     */
    public static SelectionStatus getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        
        for (SelectionStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     *
     * @param value 状态值
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
     * 判断是否可以重新提交
     *
     * @return 可以重新提交返回true
     */
    public boolean canResubmit() {
        return this == REJECTED;
    }

    /**
     * 判断是否为活跃状态（可以继续流程）
     *
     * @return 活跃状态返回true
     */
    public boolean isActive() {
        return this != REJECTED && this != CONFIRMED;
    }
}
