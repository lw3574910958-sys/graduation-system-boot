package com.lw.graduation.api.vo.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档信息视图对象 (View Object)
 * 用于向外部（如前端）展示文档的详细信息。
 * 对应领域层的 BizDocument 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "文档信息视图对象")
public class DocumentVO {

    /**
     * 文档ID
     */
    private Long id;

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

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime reviewedAt;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private Long reviewerId;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String feedback;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime uploadedAt;

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