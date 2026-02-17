package com.lw.graduation.api.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档审核DTO
 * 用于文档审核的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "文档审核请求DTO")
public class DocumentReviewDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long documentId;

    /**
     * 审核结果
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果: 1-通过, 2-驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewStatus;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String feedback;

    /**
     * 审核建议（补充说明）
     */
    @Schema(description = "审核建议")
    private String suggestion;
}