package com.lw.graduation.api.dto.selection;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 选题分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "选题分页查询参数")
public class SelectionPageQueryDTO extends BasePageQueryDTO {

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
    @Schema(description = "状态")
    private Integer status;
}