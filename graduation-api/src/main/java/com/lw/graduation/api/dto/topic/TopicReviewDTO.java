package com.lw.graduation.api.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 题目审核 DTO
 * 用于院系管理员审核题目
 *
 * @author lw
 */
@Data
@Schema(description = "题目审核请求 DTO")
public class TopicReviewDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目 ID
     */
    @NotNull(message = "题目 ID 不能为空")
    @Schema(description = "题目 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;

    /**
     * 审核结果：1-通过，2-驳回
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：1-通过，2-驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewResult;

    /**
     * 审核意见（可选）
     */
    @Schema(description = "审核意见")
    private String reviewComment;
}
