package com.lw.graduation.domain.entity.grade;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 成绩表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
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
    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
