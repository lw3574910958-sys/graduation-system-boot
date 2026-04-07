package com.lw.graduation.domain.enums.status;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目审核状态筛选条件枚举
 * 用于前端传递审核状态筛选参数
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicReviewFilter implements IEnum<String> {

    /**
     * 教师视角 - 审核通过
     */
    TEACHER_APPROVED("1", "审核通过"),

    /**
     * 教师视角 - 审核驳回
     */
    TEACHER_REJECTED("2", "审核驳回"),

    /**
     * 管理员视角 - 待审核
     */
    ADMIN_PENDING("pending", "待审核"),

    /**
     * 管理员视角 - 已审核
     */
    ADMIN_REVIEWED("reviewed", "已审核");

    /**
     * Code（前端传递值）
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;
}
