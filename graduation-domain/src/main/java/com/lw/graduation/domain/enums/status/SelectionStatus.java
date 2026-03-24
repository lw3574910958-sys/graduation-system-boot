package com.lw.graduation.domain.enums.status;

import com.lw.graduation.common.enums.IEnum;
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
public enum SelectionStatus implements IEnum<Integer> {

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
     * Code（数据库存储值）
     */
    private final Integer code;
    /**
     * 描述
     */
    private final String description;

    

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
