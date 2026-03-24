package com.lw.graduation.domain.enums.status;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目状态枚举
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicStatus implements IEnum<Integer> {

    /**
     * 草稿状态 - 教师创建题目后未提交审核或被驳回后待修改
     */
    DRAFT(0, "草稿"),
    
    /**
     * 审核中状态 - 等待院系管理员审核
     */
    REVIEWING(1, "审核中"),
    
    /**
     * 开放状态 - 审核通过，可供学生选择
     */
    OPEN(2, "开放"),
    
    /**
     * 关闭状态 - 题目不再接受选题
     */
    CLOSED(3, "关闭");

    /**
     * Code（数据库存储值）
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String description;

    

    /**
     * 判断是否为可选状态
     * 注意：还需结合 selected_count < max_selections 判断是否已满员
     *
     * @return 可选返回 true
     */
    public boolean isSelectable() {
        return this == OPEN;
    }

    /**
     * 判断是否为活跃状态（非终态）
     *
     * @return 活跃返回 true
     */
    public boolean isActive() {
        return this != CLOSED;
    }
    
    /**
     * 判断是否为草稿状态
     *
     * @return 草稿返回 true
     */
    public boolean isDraft() {
        return this == DRAFT;
    }
    
    /**
     * 判断是否为审核中状态
     *
     * @return 审核中返回 true
     */
    public boolean isReviewing() {
        return this == REVIEWING;
    }
    
    /**
     * 判断是否为开放状态
     *
     * @return 开放返回 true
     */
    public boolean isOpen() {
        return this == OPEN;
    }
    
    /**
     * 判断是否为关闭状态
     *
     * @return 关闭返回 true
     */
    public boolean isClosed() {
        return this == CLOSED;
    }
}
