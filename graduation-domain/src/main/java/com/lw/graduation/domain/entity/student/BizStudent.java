package com.lw.graduation.domain.entity.student;

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
 * 学生表
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Data
@TableName("biz_student")
public class BizStudent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联 sys_user.id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 学号
     */
    @TableField("student_id")
    private String studentId;

    /**
     * 所属院系ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 性别: 0-女, 1-男
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 专业
     */
    @TableField("major")
    private String major;

    /**
     * 班级
     */
    @TableField("class_name")
    private String className;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

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
