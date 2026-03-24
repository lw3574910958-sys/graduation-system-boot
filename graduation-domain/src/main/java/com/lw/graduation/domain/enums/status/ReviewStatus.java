package com.lw.graduation.domain.enums.status;

import com.lw.graduation.common.enums.IEnum;
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
public enum ReviewStatus implements IEnum<Integer> {

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
     * 判断是否可以重新提交审核
     *
     * @return 可以重新提交返回true
     */
    public boolean canResubmit() {
        return this == REJECTED;
    }
}
