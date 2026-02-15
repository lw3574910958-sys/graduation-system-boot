package com.lw.graduation.api.vo.topic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课题信息视图对象 (View Object)
 * 用于向外部（如前端）展示课题的详细信息。
 * 对应领域层的 BizTopic 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "课题信息视图对象")
public class TopicVO {

    /**
     * 课题ID
     */
    private Long id;

    /**
     * 课题标题
     */
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

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD) // 格式化时间输出
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;
}