package com.lw.graduation.api.dto.selection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建选题 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "创建选题参数")
public class SelectionCreateDTO {

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private Long studentId;

    /**
     * 课题ID
     */
    @NotNull(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private Long topicId;

    /**
     * 状态 (0-待确认, 1-已确认)
     */
    @Schema(description = "状态 (0-待确认, 1-已确认)", defaultValue = "0")
    private Integer status = 0; // 默认待确认
}