package com.lw.graduation.api.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新文档 DTO
 *
 * @author lw
 */
@Data
@Schema(description = "更新文档参数")
public class DocumentUpdateDTO {

    /**
     * 上传人ID
     */
    @Schema(description = "上传人ID")
    private Long userId;

    /**
     * 关联题目ID
     */
    @Schema(description = "关联题目ID")
    private Long topicId;

    /**
     * 文件类型: 0-开题报告, 1-中期报告, 2-毕业论文
     */
    @Schema(description = "文件类型: 0-开题报告, 1-中期报告, 2-毕业论文")
    private Integer fileType;

    /**
     * 原始文件名
     */
    @NotBlank(message = "原始文件名不能为空")
    @Schema(description = "原始文件名")
    private String originalFilename;

    /**
     * 服务器存储路径
     */
    @Schema(description = "服务器存储路径")
    private String storedPath;

    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    /**
     * 审核状态: 0-待审, 1-通过, 2-驳回
     */
    @Schema(description = "审核状态: 0-待审, 1-通过, 2-驳回")
    private Integer reviewStatus;
}