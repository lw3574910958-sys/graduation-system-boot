package com.lw.graduation.api.dto.selection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新选题 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "更新选题参数")
public class SelectionUpdateDTO {

    /**
     * 学生ID
     */
    @Schema(description = "学生ID")
    private Long studentId;

    /**
     * 课题ID
     */
    @Schema(description = "课题ID")
    private Long topicId;

    /**
     * 状态 (0-待确认, 1-已确认)
     */
    @Schema(description = "状态 (0-待确认, 1-已确认)")
    private Integer status;
}