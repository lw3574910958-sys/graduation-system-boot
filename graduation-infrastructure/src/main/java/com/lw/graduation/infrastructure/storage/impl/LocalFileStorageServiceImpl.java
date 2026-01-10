package com.lw.graduation.infrastructure.storage.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.lw.graduation.infrastructure.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/** 
 * 本地文件存储服务实现
 * 将文件存储到本地磁盘，按照日期目录组织文件结构
 * 包含安全验证措施，防止恶意文件上传
 *
 * @author lw
 */
@Component
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.dir:D:/Project/myapps/data/uploadFiles}")
    private String uploadDir;
    
    // 允许的文件扩展名列表
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt"
    );
    
    // 允许的图片 MIME 类型
    private static final List<String> ALLOWED_IMAGE_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    /**
     * 存储文件
     */
    @Override
    public String store(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        
        // 验证文件类型
        validateFile(file);
        
        // 防止路径遍历攻击
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null && (originalFileName.contains("../") || originalFileName.contains("..\\"))) {
            throw new IllegalArgumentException("非法的文件名，可能包含路径遍历攻击");
        }

        // 创建目标目录
        String datePath = DateUtil.format(java.util.Date.from(java.time.Instant.now()), "yyyy/MM/dd");
        String targetDir = Paths.get(uploadDir, category, datePath).toString();
        FileUtil.mkdir(targetDir);

        // 生成唯一文件名
        String extension = FileUtil.extName(originalFileName);
        String newFileName = IdUtil.fastSimpleUUID() + "." + extension.toLowerCase();
        String targetPath = Paths.get(targetDir, newFileName).toString();

        // 保存文件
        file.transferTo(Paths.get(targetPath).toFile());

        // 返回相对于上传根目录的路径
        String relativePath = Paths.get(category, datePath, newFileName).toString().replace("\\", "/");
        log.info("文件上传成功: {}", relativePath);
        
        return relativePath;
    }
    
    /**
     * 验证上传的文件是否安全
     */
    private void validateFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = FileUtil.extName(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不允许的文件类型: " + extension + ", 仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
        
        // 检查文件的 MIME 类型
        String contentType = file.getContentType();
        if (contentType != null && (extension.equals("jpg") || extension.equals("jpeg") || 
             extension.equals("png") || extension.equals("gif"))) {
            if (!ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException("非法的图片文件");
            }
        }
        
        // 检查文件大小（例如限制为10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超出限制，最大支持10MB");
        }
    }
}