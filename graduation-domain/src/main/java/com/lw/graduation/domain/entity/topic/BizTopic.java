package com.lw.graduation.domain.entity.topic;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 题目表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Data
@TableName("biz_topic")
public class BizTopic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题目标题
     */
    @TableField("title")
    private String title;

    /**
     * 题目描述
     */
    @TableField("description")
    private String description;

    /**
     * 发布教师ID(biz_teacher.id)
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 状态: 1-开放, 2-已选, 3-关闭
     */
    @TableField("status")
    private Integer status;

    /**
     * 所属院系ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 题目来源
     */
    @TableField("source")
    private String source;

    /**
     * 题目类型
     */
    @TableField("type")
    private String type;

    /**
     * 题目性质
     */
    @TableField("nature")
    private String nature;

    /**
     * 预计难度(1-5)
     */
    @TableField("difficulty")
    private Integer difficulty;

    /**
     * 预计工作量(1-5)
     */
    @TableField("workload")
    private Integer workload;

    /**
     * 选题人数限制
     */
    @TableField("max_selections")
    private Integer maxSelections;

    /**
     * 已选人数
     */
    @TableField("selected_count")
    private Integer selectedCount;

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
}
