  package com.lw.graduation.domain.enums.grade;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 成绩类型枚举
 * 定义毕业设计系统中不同类型的评分项目
 * 评分基于学生提交的三类文档：开题报告、中期报告、毕业论文
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum GradeType implements IEnum<Integer> {

    /**
     * 开题报告教师评分 30%
     */
    PROPOSAL_GRADE(0, "开题报告教师评分", new BigDecimal("0.3")),
    
    /**
     * 中期报告教师评分 30%
     */
    MIDTERM_GRADE(1, "中期报告教师评分", new BigDecimal("0.3")),
    
    /**
     * 毕业论文教师评分 40%
     */
    THESIS_GRADE(2, "毕业论文教师评分", new BigDecimal("0.4")),
    
    /**
     * 综合成绩（自动计算）
     */
    COMPOSITE_GRADE(3, "综合成绩", null);

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
        return this == PROPOSAL_GRADE || this == MIDTERM_GRADE || this == THESIS_GRADE;
    }
}
