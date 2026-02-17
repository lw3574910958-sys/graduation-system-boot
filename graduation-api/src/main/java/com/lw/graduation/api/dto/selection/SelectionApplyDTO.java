package com.lw.graduation.api.dto.selection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 选题申请DTO
 * 用于学生申请选题的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "选题申请DTO")
public class SelectionApplyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;

    /**
     * 申请理由
     */
    @Schema(description = "申请理由")
    private String applyReason;

    /**
     * 学生能力说明
     */
    @Schema(description = "学生能力说明")
    private String studentAbility;

    /**
     * 预期目标
     */
    @Schema(description = "预期目标")
    private String expectedGoal;
}