package com.lw.graduation.api.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 题目更新DTO
 * 用于教师修改已有题目的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "题目更新请求DTO")
public class TopicUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

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
    @Schema(description = "选题人数限制")
    private Integer maxSelections;

    /**
     * 题目状态
     */
    @Schema(description = "题目状态: 0-新建, 1-开放, 2-已选, 3-关闭")
    private Integer status;
}