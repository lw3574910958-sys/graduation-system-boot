package com.lw.graduation.grade.service.calculator;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩计算服务接口
 * 定义成绩计算、统计分析等核心算法
 *
 * @author lw
 */
public interface GradeCalculatorService {

    /**
     * 计算加权平均成绩
     *
     * @param scores 成绩列表
     * @param weights 权重列表（与scores一一对应）
     * @return 加权平均成绩
     */
    BigDecimal calculateWeightedAverage(List<BigDecimal> scores, List<BigDecimal> weights);

    /**
     * 计算简单平均成绩
     *
     * @param scores 成绩列表
     * @return 平均成绩
     */
    BigDecimal calculateAverage(List<BigDecimal> scores);

    /**
     * 计算总成绩（各部分成绩求和）
     *
     * @param scores 成绩列表
     * @return 总成绩
     */
    BigDecimal calculateTotal(List<BigDecimal> scores);

    /**
     * 根据成绩获取绩点
     *
     * @param score 成绩
     * @return 绩点
     */
    BigDecimal calculateGPA(BigDecimal score);

    /**
     * 计算多个科目的平均绩点
     *
     * @param scores 成绩列表
     * @return 平均绩点
     */
    BigDecimal calculateAverageGPA(List<BigDecimal> scores);

    /**
     * 判断成绩是否及格
     *
     * @param score 成绩
     * @return 及格返回true
     */
    boolean isPassing(BigDecimal score);

    /**
     * 获取成绩等级
     *
     * @param score 成绩
     * @return 成绩等级描述
     */
    String getGradeLevel(BigDecimal score);

    /**
     * 计算排名百分比
     *
     * @param score 当前成绩
     * @param allScores 所有成绩列表
     * @return 排名百分比（0-100）
     */
    BigDecimal calculatePercentileRank(BigDecimal score, List<BigDecimal> allScores);

    /**
     * 统计成绩分布
     *
     * @param scores 成绩列表
     * @return 各等级人数统计
     */
    GradeDistribution calculateDistribution(List<BigDecimal> scores);
}