package com.lw.graduation.grade.service.calculator;

import com.lw.graduation.domain.enums.grade.GradeLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 成绩分布统计
 * 用于统计各等级成绩的人数分布情况
 *
 * @author lw
 */
@Data
public class GradeDistribution {

    /**
     * 优秀人数 (90-100分)
     */
    private int excellentCount = 0;
    
    /**
     * 良好人数 (80-89分)
     */
    private int goodCount = 0;
    
    /**
     * 中等人数 (70-79分)
     */
    private int fairCount = 0;
    
    /**
     * 及格人数 (60-69分)
     */
    private int passCount = 0;
    
    /**
     * 不及格人数 (0-59分)
     */
    private int failCount = 0;
    
    /**
     * 总人数
     */
    private int totalCount = 0;
    
    /**
     * 及格率 (百分比)
     */
    private BigDecimal passRate = BigDecimal.ZERO;
    
    /**
     * 平均分
     */
    private BigDecimal averageScore = BigDecimal.ZERO;
    
    /**
     * 最高分
     */
    private BigDecimal highestScore = BigDecimal.ZERO;
    
    /**
     * 最低分
     */
    private BigDecimal lowestScore = BigDecimal.ZERO;

    /**
     * 添加成绩到统计中
     *
     * @param score 成绩
     */
    public void addScore(BigDecimal score) {
        if (score == null) {
            return;
        }
        
        totalCount++;
        
        // 使用GradeLevel枚举的containsScore方法进行等级判断
        if (GradeLevel.EXCELLENT.containsScore(score)) {
            excellentCount++;
        } else if (GradeLevel.GOOD.containsScore(score)) {
            goodCount++;
        } else if (GradeLevel.FAIR.containsScore(score)) {
            fairCount++;
        } else if (GradeLevel.PASS.containsScore(score)) {
            passCount++;
        } else if (GradeLevel.FAIL.containsScore(score)) {
            failCount++;
        }
        
        // 更新最高分和最低分
        if (totalCount == 1) {
            highestScore = score;
            lowestScore = score;
        } else {
            if (score.compareTo(highestScore) > 0) {
                highestScore = score;
            }
            if (score.compareTo(lowestScore) < 0) {
                lowestScore = score;
            }
        }
    }

    /**
     * 计算及格率
     */
    public void calculatePassRate() {
        if (totalCount > 0) {
            BigDecimal passingCount = new BigDecimal(excellentCount + goodCount + fairCount + passCount);
            passRate = passingCount.multiply(new BigDecimal("100"))
                                  .divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 获取指定等级的比例
     *
     * @param level 等级标识 (excellent, good, fair, pass, fail)
     * @return 比例 (百分比)
     */
    public BigDecimal getLevelPercentage(String level) {
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }
        
        int count = switch (level.toLowerCase()) {
            case "excellent" -> excellentCount;
            case "good" -> goodCount;
            case "fair" -> fairCount;
            case "pass" -> passCount;
            case "fail" -> failCount;
            default -> 0;
        };
        
        return new BigDecimal(count).multiply(new BigDecimal("100"))
                                   .divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
    }
}