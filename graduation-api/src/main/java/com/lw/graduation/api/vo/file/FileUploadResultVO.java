package com.lw.graduation.api.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传结果视图对象
 *
 * @author lw
 */
@Data
@Schema(description = "文件上传结果")
public class FileUploadResultVO implements Serializable {

    @Schema(description = "文件原始名称")
    private String originalName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String contentType;

    @Schema(description = "存储路径")
    private String storedPath;

    @Schema(description = "访问URL")
    private String url;

    @Schema(description = "文件扩展名")
    private String extension;

    @Schema(description = "上传时间")
    private String uploadTime;

    public static FileUploadResultVO of(String originalName, Long fileSize, String contentType, 
                                       String storedPath, String url, String extension, String uploadTime) {
        FileUploadResultVO vo = new FileUploadResultVO();
        vo.originalName = originalName;
        vo.fileSize = fileSize;
        vo.contentType = contentType;
        vo.storedPath = storedPath;
        vo.url = url;
        vo.extension = extension;
        vo.uploadTime = uploadTime;
        return vo;
    }
}