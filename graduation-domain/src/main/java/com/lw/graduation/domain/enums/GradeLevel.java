package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 成绩等级枚举
 * 定义成绩的等级划分标准和对应的分数区间
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum GradeLevel {

    /**
     * 优秀 (90-100分)
     */
    EXCELLENT(new BigDecimal("90"), new BigDecimal("100"), "优秀", new BigDecimal("4.0")),
    
    /**
     * 良好 (80-89分)
     */
    GOOD(new BigDecimal("80"), new BigDecimal("89"), "良好", new BigDecimal("3.0")),
    
    /**
     * 中等 (70-79分)
     */
    FAIR(new BigDecimal("70"), new BigDecimal("79"), "中等", new BigDecimal("2.0")),
    
    /**
     * 及格 (60-69分)
     */
    PASS(new BigDecimal("60"), new BigDecimal("69"), "及格", new BigDecimal("1.0")),
    
    /**
     * 不及格 (0-59分)
     */
    FAIL(new BigDecimal("0"), new BigDecimal("59"), "不及格", new BigDecimal("0.0"));

    /**
     * 最低分数
     */
    private final BigDecimal minScore;
    
    /**
     * 最高分数
     */
    private final BigDecimal maxScore;
    
    /**
     * 等级描述
     */
    private final String description;
    
    /**
     * 对应绩点
     */
    private final BigDecimal gpa;

    /**
     * 根据分数获取成绩等级
     *
     * @param score 分数
     * @return 对应的成绩等级，未找到返回null
     */
    public static GradeLevel getByScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        
        for (GradeLevel level : values()) {
            if (score.compareTo(level.minScore) >= 0 && score.compareTo(level.maxScore) <= 0) {
                return level;
            }
        }
        return null;
    }

    /**
     * 判断分数是否在该等级范围内
     *
     * @param score 分数
     * @return 在范围内返回true
     */
    public boolean containsScore(BigDecimal score) {
        if (score == null) {
            return false;
        }
        return score.compareTo(minScore) >= 0 && score.compareTo(maxScore) <= 0;
    }

    /**
     * 获取等级的平均绩点
     *
     * @return 平均绩点
     */
    public BigDecimal getAverageGPA() {
        // 简单的线性插值计算
        if (this == EXCELLENT) {
            return new BigDecimal("3.7"); // 90-100分平均绩点
        } else if (this == GOOD) {
            return new BigDecimal("2.7"); // 80-89分平均绩点
        } else if (this == FAIR) {
            return new BigDecimal("1.7"); // 70-79分平均绩点
        } else if (this == PASS) {
            return new BigDecimal("0.7"); // 60-69分平均绩点
        } else {
            return BigDecimal.ZERO; // 不及格
        }
    }

    /**
     * 判断是否为及格等级
     *
     * @return 及格返回true
     */
    public boolean isPassing() {
        return this != FAIL;
    }

    /**
     * 判断是否为优秀等级
     *
     * @return 优秀返回true
     */
    public boolean isExcellent() {
        return this == EXCELLENT;
    }
}