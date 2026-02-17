package com.lw.graduation.api.dto.selection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 选题审核DTO
 * 用于教师审核学生选题申请的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "选题审核DTO")
public class SelectionReviewDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 选题ID
     */
    @NotNull(message = "选题ID不能为空")
    @Schema(description = "选题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long selectionId;

    /**
     * 审核结果
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果: 1-通过, 2-驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewResult;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComment;

    /**
     * 建议修改内容
     */
    @Schema(description = "建议修改内容")
    private String suggestedChanges;

    /**
     * 备注说明
     */
    @Schema(description = "备注说明")
    private String remark;
}