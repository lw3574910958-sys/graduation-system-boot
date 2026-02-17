package com.lw.graduation.api.service.file.impl;

import cn.hutool.core.date.DateUtil;
import com.lw.graduation.api.service.file.UnifiedFileUploadService;
import com.lw.graduation.api.vo.file.FileUploadResultVO;
import com.lw.graduation.common.enums.FileType;
import com.lw.graduation.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 统一文件上传服务实现
 * 集中处理各种文件上传业务逻辑
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedFileUploadServiceImpl implements UnifiedFileUploadService {

    private final FileStorageService fileStorageService;

    @Override
    public FileUploadResultVO uploadFile(MultipartFile file, String category) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 验证文件类型
        FileType.ValidationResult result = FileType.validate(extension, file.getSize());
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getMessage());
        }

        // 存储文件
        String storedPath = fileStorageService.store(file, category);
        
        // 构建返回结果
        return buildResult(file, storedPath);
    }

    @Override
    public FileUploadResultVO uploadAvatar(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("头像文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 验证是否为图片类型
        FileType fileType = FileType.getByExtension(extension);
        if (fileType == null || fileType.getCategory() != FileType.Category.IMAGE) {
            throw new IllegalArgumentException("仅支持图片格式文件");
        }

        // 验证文件大小（头像限制为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("头像文件大小不能超过2MB");
        }

        // 存储头像文件
        String category = "avatar/" + userId;
        String storedPath = fileStorageService.store(file, category, "avatar");
        
        // 构建返回结果
        return buildResult(file, storedPath);
    }

    @Override
    public FileUploadResultVO uploadDocument(MultipartFile file, Long topicId, Integer fileType, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文档文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 验证文件类型
        FileType.ValidationResult result = FileType.validate(extension, file.getSize());
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getMessage());
        }

        // 验证文档类型是否为允许的文档格式
        FileType docFileType = FileType.getByExtension(extension);
        if (docFileType == null || 
            (docFileType.getCategory() != FileType.Category.DOCUMENT && 
             docFileType.getCategory() != FileType.Category.SPREADSHEET &&
             docFileType.getCategory() != FileType.Category.PRESENTATION)) {
            throw new IllegalArgumentException("仅支持文档、表格、演示文稿格式文件");
        }

        // 存储文档文件
        String category = "document/topic_" + topicId + "/" + fileType;
        String storedPath = fileStorageService.store(file, category);
        
        // 构建返回结果
        return buildResult(file, storedPath);
    }

    @Override
    public boolean deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        boolean deleted = fileStorageService.delete(filePath);
        if (deleted) {
            log.info("文件删除成功: {}", filePath);
        }
        return deleted;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 构建上传结果
     */
    private FileUploadResultVO buildResult(MultipartFile file, String storedPath) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        return FileUploadResultVO.of(
            originalFilename,
            file.getSize(),
            file.getContentType(),
            storedPath,
            fileStorageService.getUrl(storedPath),
            extension,
            DateUtil.now()
        );
    }
}