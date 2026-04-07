package com.lw.graduation.domain.enums.notice;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知目标范围枚举
 * 定义通知公告的可见范围
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TargetScope implements IEnum<Integer> {

    /**
     * 全体 - 对所有用户可见
     */
    ALL(0, "全体"),

    /**
     * 学生 - 仅对学生可见
     */
    STUDENT(1, "学生"),

    /**
     * 教师 - 仅对教师可见
     */
    TEACHER(2, "教师"),

    /**
     * 院系管理员 - 仅对院系管理员可见
     */
    DEPARTMENT_ADMIN(3, "院系管理员");

    /**
     * Code（数据库存储值）
     */
    private final Integer code;

    /**
     * 描述
     */
    private final String description;

}
