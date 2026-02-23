package com.lw.graduation.api.dto.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档上传DTO
 * 用于文档上传的数据传输对象
 *
 * @author lw
 */
@Data
@Schema(description = "文档上传请求DTO")
public class DocumentUploadDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联题目ID
     */
    @NotNull(message = "题目ID不能为空")
    @Schema(description = "关联题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;

    /**
     * 文件类型
     */
    @NotNull(message = "文件类型不能为空")
    @Schema(description = "文件类型: 0-开题报告, 1-中期报告, 2-毕业论文, 3-外文翻译, 4-其他文档",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer fileType;

    /**
     * 上传的文件
     */
    @NotNull(message = "文件不能为空")
    @Schema(description = "上传的文件", requiredMode = Schema.RequiredMode.REQUIRED)
    private MultipartFile file;

    /**
     * 文档描述
     */
    @Schema(description = "文档描述")
    private String description;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String version;
}