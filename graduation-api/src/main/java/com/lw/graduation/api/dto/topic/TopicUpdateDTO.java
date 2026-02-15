package com.lw.graduation.api.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新课题 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "更新课题参数")
public class TopicUpdateDTO {

    /**
     * 课题标题
     */
    @NotBlank(message = "课题标题不能为空")
    @Schema(description = "课题标题")
    private String title;

    /**
     * 课题描述
     */
    @Schema(description = "课题描述")
    private String description;

    /**
     * 指导教师ID
     */
    @Schema(description = "指导教师ID")
    private Long teacherId;

    /**
     * 状态 (0-开放, 1-已选, 2-关闭)
     */
    @Schema(description = "状态 (0-开放, 1-已选, 2-关闭)")
    private Integer status;
}