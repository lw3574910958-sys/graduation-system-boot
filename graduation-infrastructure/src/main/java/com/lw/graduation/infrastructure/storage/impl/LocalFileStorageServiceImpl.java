package com.lw.graduation.infrastructure.storage.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.lw.graduation.common.enums.FileType;
import com.lw.graduation.infrastructure.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储服务实现
 * 将文件存储到本地磁盘，按照日期目录组织文件结构
 * 使用统一的文件类型验证和安全管理
 *
 * @author lw
 */
@Component
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${file.storage.base-path:${file.dir:${UPLOAD_DIR:D:/Project/myapps/graduation-system/data/uploadFiles}}}")
    private String basePath;

    @Value("${file.storage.url-prefix:/files}")
    private String urlPrefix;

    /**
     * 存储文件
     */
    @Override
    public String store(MultipartFile file, String category) throws IOException {
        return store(file, category, null);
    }

    /**
     * 存储文件（指定文件名）
     */
    @Override
    public String store(MultipartFile file, String category, String filename) throws IOException {
        validateInput(file, category);
        
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        
        // 验证文件类型
        FileType.ValidationResult result = FileType.validate(extension, file.getSize());
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getMessage());
        }
        
        // 生成文件名
        String finalFilename = generateFilename(filename, extension);
        
        // 生成存储路径
        String datePath = DateUtil.format(java.util.Date.from(java.time.Instant.now()), "yyyy/MM/dd");
        String relativePath = Paths.get(category, datePath, finalFilename).toString().replace("\\", "/");
        Path fullPath = Paths.get(basePath, relativePath);
        
        // 创建目录并保存文件
        Files.createDirectories(fullPath.getParent());
        file.transferTo(fullPath);
        
        log.info("文件上传成功: {} -> {}", originalFilename, relativePath);
        return relativePath;
    }

    /**
     * 通过输入流存储文件
     */
    @Override
    public String storeStream(InputStream inputStream, String category, String filename) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("文件分类不能为空");
        }
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 验证文件扩展名
        String extension = FileUtil.extName(filename);
        if (extension == null || extension.isEmpty()) {
            throw new IllegalArgumentException("文件名必须包含扩展名");
        }
        
        FileType.ValidationResult result = FileType.validate(extension, 0); // 流大小未知，只验证类型
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getMessage());
        }
        
        // 生成存储路径
        String datePath = DateUtil.format(java.util.Date.from(java.time.Instant.now()), "yyyy/MM/dd");
        String relativePath = Paths.get(category, datePath, filename).toString().replace("\\", "/");
        Path fullPath = Paths.get(basePath, relativePath);
        
        // 创建目录并保存文件
        Files.createDirectories(fullPath.getParent());
        Files.copy(inputStream, fullPath);
        
        log.info("文件流上传成功: {}", relativePath);
        return relativePath;
    }

    /**
     * 下载文件
     */
    @Override
    public InputStream download(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        
        Path fullPath = Paths.get(basePath, filePath);
        if (!Files.exists(fullPath)) {
            throw new IOException("文件不存在: " + filePath);
        }
        
        return Files.newInputStream(fullPath);
    }

    /**
     * 删除文件
     */
    @Override
    public boolean delete(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        Path fullPath = Paths.get(basePath, filePath);
        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
            log.info("文件删除成功: {}", filePath);
            return true;
        }
        return false;
    }

    /**
     * 检查文件是否存在
     */
    @Override
    public boolean exists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        Path fullPath = Paths.get(basePath, filePath);
        return Files.exists(fullPath);
    }

    /**
     * 获取文件完整访问URL
     */
    @Override
    public String getUrl(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        return urlPrefix + "/" + filePath;
    }

    /**
     * 获取文件绝对路径
     */
    @Override
    public String getAbsolutePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        return Paths.get(basePath, filePath).toAbsolutePath().toString();
    }

    /**
     * 获取存储类型标识
     */
    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * 验证输入参数
     */
    private void validateInput(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("文件分类不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 防止路径遍历攻击
        if (originalFilename.contains("../") || originalFilename.contains("..\\")) {
            throw new IllegalArgumentException("非法的文件名，可能包含路径遍历攻击");
        }
    }

    /**
     * 生成文件名
     */
    private String generateFilename(String customName, String extension) {
        if (customName != null && !customName.trim().isEmpty()) {
            return customName + "." + extension.toLowerCase();
        }
        return IdUtil.fastSimpleUUID() + "." + extension.toLowerCase();
    }
}