package com.lw.graduation.domain.entity.grade;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.domain.enums.grade.GradeLevel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩表
 * 用于管理毕业设计的成绩评定，包括指导教师评分、答辩评分等。
 * 支持成绩计算、等级评定、绩点转换等功能。
 */
@Data
@TableName("biz_grade")
public class BizGrade implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID(biz_student.id)
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 题目ID(biz_topic.id)
     */
    @TableField("topic_id")
    private Long topicId;

    /**
     * 成绩(0.00 ~ 100.00)
     */
    @TableField("score")
    private BigDecimal score;

    /**
     * 评分教师ID(sys_user.id)
     */
    @TableField("grader_id")
    private Long graderId;

    /**
     * 评语
     */
    @TableField("comment")
    private String comment;

    /**
     * 评分时间
     */
    @TableField("graded_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gradedAt;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 获取成绩等级
     *
     * @return 成绩等级
     */
    public String getGradeLevel() {
        if (this.score == null) {
            return "未评分";
        }
        
        // 使用成绩等级枚举进行判断
        GradeLevel level = GradeLevel.getByScore(this.score);
        return level != null ? level.getDescription() : "未知等级";
    }

    /**
     * 检查成绩是否及格
     *
     * @return 及格返回true
     */
    public boolean isPass() {
        if (this.score == null) {
            return false;
        }
        
        // 使用成绩等级枚举判断
        GradeLevel level = GradeLevel.getByScore(this.score);
        return level != null && level.isPassing();
    }

    /**
     * 检查是否为优秀成绩
     *
     * @return 优秀返回true
     */
    public boolean isExcellent() {
        if (this.score == null) {
            return false;
        }
        
        // 使用成绩等级枚举判断
        GradeLevel level = GradeLevel.getByScore(this.score);
        return level != null && level.isExcellent();
    }

    /**
     * 获取绩点
     *
     * @return 绩点值
     */
    public BigDecimal getGPA() {
        if (this.score == null) {
            return BigDecimal.ZERO;
        }
        
        // 使用成绩等级枚举计算绩点
        GradeLevel level = GradeLevel.getByScore(this.score);
        return level != null ? level.getGpa() : BigDecimal.ZERO;
    }
}
