package com.lw.graduation.api.dto.selection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
     * 题目 ID
     */
    @NotNull(message = "题目 ID 不能为空")
    @Schema(description = "题目 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;
    
    /**
     * 申请理由
     */
    @NotBlank(message = "申请理由不能为空")
    @Size(min = 10, max = 500, message = "申请理由长度应在 10-500 个字符之间")
    @Schema(description = "申请理由", example = "我对这个课题很感兴趣，具备相关基础...")
    private String applyReason;
    
    /**
     * 学生能力说明
     */
    @NotBlank(message = "学生能力说明不能为空")
    @Size(min = 10, max = 500, message = "学生能力说明长度应在 10-500 个字符之间")
    @Schema(description = "学生能力说明", example = "我熟悉 Java 编程，掌握 Spring Boot 框架...")
    private String studentAbility;
    
    /**
     * 预期目标
     */
    @NotBlank(message = "预期目标不能为空")
    @Size(min = 10, max = 500, message = "预期目标长度应在 10-500 个字符之间")
    @Schema(description = "预期目标", example = "希望通过本课题提升系统设计能力和工程实践能力...")
    private String expectedGoal;
}