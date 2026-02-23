package com.lw.graduation.grade.service.calculator.impl;

import com.lw.graduation.domain.enums.grade.GradeLevel;
import com.lw.graduation.grade.service.calculator.GradeCalculatorService;
import com.lw.graduation.grade.service.calculator.GradeDistribution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 成绩计算服务实现类
 * 实现各种成绩计算和统计分析算法
 *
 * @author lw
 */
@Service
@Slf4j
public class GradeCalculatorServiceImpl implements GradeCalculatorService {

    private static final int SCALE = 2; // 计算精度
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP; // 四舍五入

    @Override
    public BigDecimal calculateWeightedAverage(List<BigDecimal> scores, List<BigDecimal> weights) {
        if (scores == null || weights == null || scores.size() != weights.size() || scores.isEmpty()) {
            throw new IllegalArgumentException("成绩和权重列表不能为空且长度必须相等");
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;

        for (int i = 0; i < scores.size(); i++) {
            BigDecimal score = scores.get(i);
            BigDecimal weight = weights.get(i);

            if (score != null && weight != null) {
                weightedSum = weightedSum.add(score.multiply(weight));
                weightSum = weightSum.add(weight);
            }
        }

        if (weightSum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return weightedSum.divide(weightSum, SCALE, ROUNDING_MODE);
    }

    @Override
    public BigDecimal calculateAverage(List<BigDecimal> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        int validCount = 0;

        for (BigDecimal score : scores) {
            if (score != null) {
                sum = sum.add(score);
                validCount++;
            }
        }

        if (validCount == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(new BigDecimal(validCount), SCALE, ROUNDING_MODE);
    }

    @Override
    public BigDecimal calculateTotal(List<BigDecimal> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal score : scores) {
            if (score != null) {
                sum = sum.add(score);
            }
        }

        return sum;
    }

    @Override
    public BigDecimal calculateGPA(BigDecimal score) {
        if (score == null) {
            return BigDecimal.ZERO;
        }

        // 优先使用GradeLevel枚举的getAverageGPA方法
        GradeLevel level = GradeLevel.getByScore(score);
        if (level != null) {
            return level.getAverageGPA();
        }

        // 如果枚举方法不可用，则使用传统区间判断
        return switch (score.intValue() / 10) {
            case 10, 9 -> new BigDecimal("4.0"); // 90-100
            case 8 -> score.compareTo(new BigDecimal("85")) >= 0 ?
                     new BigDecimal("3.7") : new BigDecimal("3.3"); // 85-89 vs 80-84
            case 7 -> score.compareTo(new BigDecimal("75")) >= 0 ?
                     (score.compareTo(new BigDecimal("78")) >= 0 ? new BigDecimal("3.0") : new BigDecimal("2.7")) :
                     (score.compareTo(new BigDecimal("72")) >= 0 ? new BigDecimal("2.3") : new BigDecimal("2.0"));
            case 6 -> score.compareTo(new BigDecimal("60")) >= 0 ?
                     (score.compareTo(new BigDecimal("64")) >= 0 ? new BigDecimal("1.5") : new BigDecimal("1.0")) :
                     BigDecimal.ZERO;
            default -> BigDecimal.ZERO; // 0-59
        };
    }

    @Override
    public BigDecimal calculateAverageGPA(List<BigDecimal> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal gpaSum = BigDecimal.ZERO;
        int validCount = 0;

        for (BigDecimal score : scores) {
            if (score != null) {
                gpaSum = gpaSum.add(calculateGPA(score));
                validCount++;
            }
        }

        if (validCount == 0) {
            return BigDecimal.ZERO;
        }

        return gpaSum.divide(new BigDecimal(validCount), SCALE, ROUNDING_MODE);
    }

    @Override
    public boolean isPassing(BigDecimal score) {
        return score != null && score.compareTo(new BigDecimal("60")) >= 0;
    }

    @Override
    public String getGradeLevel(BigDecimal score) {
        GradeLevel level = GradeLevel.getByScore(score);
        return level != null ? level.getDescription() : "未知";
    }

    @Override
    public BigDecimal calculatePercentileRank(BigDecimal score, List<BigDecimal> allScores) {
        if (score == null || allScores == null || allScores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 统计低于当前成绩的人数
        long lowerCount = allScores.stream()
                .filter(s -> s != null && s.compareTo(score) < 0)
                .count();

        // 计算百分位排名并直接返回
        return new BigDecimal(lowerCount)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(allScores.size()), SCALE, ROUNDING_MODE);
    }

    @Override
    public GradeDistribution calculateDistribution(List<BigDecimal> scores) {
        GradeDistribution distribution = new GradeDistribution();

        if (scores == null || scores.isEmpty()) {
            return distribution;
        }

        // 添加所有成绩到统计中
        for (BigDecimal score : scores) {
            distribution.addScore(score);
        }

        // 计算平均分
        distribution.setAverageScore(calculateAverage(scores));

        // 计算及格率
        distribution.calculatePassRate();

        log.info("成绩分布统计完成: 总人数={}, 及格率={}%",
                distribution.getTotalCount(), distribution.getPassRate());

        return distribution;
    }
}