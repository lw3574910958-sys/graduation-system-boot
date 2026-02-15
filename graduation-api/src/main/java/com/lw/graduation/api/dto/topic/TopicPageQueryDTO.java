package com.lw.graduation.api.dto.topic;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课题分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课题分页查询参数")
public class TopicPageQueryDTO extends BasePageQueryDTO {

    /**
     * 课题标题（模糊查询）
     */
    @Schema(description = "课题标题")
    private String title;

    /**
     * 指导教师ID
     */
    @Schema(description = "指导教师ID")
    private Long teacherId;

    /**
     * 状态 (0-开放, 1-已选, 2-关闭)
     */
    @Schema(description = "状态")
    private Integer status;
}