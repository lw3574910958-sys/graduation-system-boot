package com.lw.graduation.domain.enums.grade;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 成绩类型枚举
 * 定义毕业设计系统中不同类型的评分项目
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum GradeType implements IEnum<Integer> {

    /**
     * 指导教师评分 40%
     */
    ADVISOR_GRADE(1, "指导教师评分", new BigDecimal("0.4")),
    
    /**
     * 评阅教师评分 30%
     */
    REVIEWER_GRADE(2, "评阅教师评分", new BigDecimal("0.3")),
    
    /**
     * 答辩成绩 30%
     */
    DEFENSE_GRADE(3, "答辩成绩", new BigDecimal("0.3")),
    
    /**
     * 综合成绩（自动计算）
     */
    COMPOSITE_GRADE(4, "综合成绩", null);

    /**
     * Code（数据库存储值）
     */
    private final Integer code;
    
    /**
     * 描述
     */
    private final String description;
    
    /**
     * 权重（仅单项成绩有）
     */
    private final BigDecimal weight;

    /**
     * 判断是否为综合成绩
     *
     * @return 综合成绩返回 true
     */
    public boolean isComposite() {
        return this == COMPOSITE_GRADE;
    }

    /**
     * 判断是否为单项成绩
     *
     * @return 单项成绩返回 true
     */
    public boolean isSingleGrade() {
        return this == ADVISOR_GRADE || this == REVIEWER_GRADE || this == DEFENSE_GRADE;
    }
}
