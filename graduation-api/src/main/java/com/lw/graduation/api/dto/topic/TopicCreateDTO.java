package com.lw.graduation.api.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 题目创建DTO
 * 用于教师发布新题目的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "题目创建请求DTO")
public class TopicCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目标题
     */
    @NotBlank(message = "题目标题不能为空")
    @Schema(description = "题目标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 题目描述
     */
    @NotBlank(message = "题目描述不能为空")
    @Schema(description = "题目描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    /**
     * 所属院系ID
     */
    @NotNull(message = "院系ID不能为空")
    @Schema(description = "所属院系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long departmentId;

    /**
     * 题目来源
     */
    @Schema(description = "题目来源")
    private String source;

    /**
     * 题目类型
     */
    @Schema(description = "题目类型")
    private String type;

    /**
     * 题目性质
     */
    @Schema(description = "题目性质")
    private String nature;

    /**
     * 预计难度(1-5)
     */
    @Schema(description = "预计难度(1-5)")
    private Integer difficulty;

    /**
     * 预计工作量(1-5)
     */
    @Schema(description = "预计工作量(1-5)")
    private Integer workload;

    /**
     * 选题人数限制
     */
    @Schema(description = "选题人数限制", defaultValue = "1")
    private Integer maxSelections = 1;
}