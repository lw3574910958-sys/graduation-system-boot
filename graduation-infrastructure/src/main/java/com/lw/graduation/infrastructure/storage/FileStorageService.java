package com.lw.graduation.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口（策略模式）
 * 支持本地存储、MinIO、OSS 等实现
 *
 * @author lw
 */
public interface FileStorageService {

    /**
     * 保存文件
     * @param file 上传的文件
     * @param category 文件分类（如 "document/opening"）
     * @return 访问 URL 或相对路径
     */
    String store(MultipartFile file, String category) throws IOException;
}
