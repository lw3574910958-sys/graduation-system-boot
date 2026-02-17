package com.lw.graduation.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 统一文件存储服务接口（策略模式）
 * 支持本地存储、MinIO、OSS 等多种存储实现
 *
 * @author lw
 */
public interface FileStorageService {

    /**
     * 保存文件
     * @param file 上传的文件
     * @param category 文件分类（如 "document/opening"）
     * @return 文件存储路径
     * @throws IOException IO异常
     */
    String store(MultipartFile file, String category) throws IOException;

    /**
     * 保存文件（指定文件名）
     * @param file 上传的文件
     * @param category 文件分类
     * @param filename 自定义文件名（不含扩展名）
     * @return 文件存储路径
     * @throws IOException IO异常
     */
    String store(MultipartFile file, String category, String filename) throws IOException;

    /**
     * 通过输入流保存文件
     * @param inputStream 文件输入流
     * @param category 文件分类
     * @param filename 文件名（包含扩展名）
     * @return 文件存储路径
     * @throws IOException IO异常
     */
    String storeStream(InputStream inputStream, String category, String filename) throws IOException;

    /**
     * 下载文件
     * @param filePath 文件存储路径
     * @return 文件输入流
     * @throws IOException IO异常
     */
    InputStream download(String filePath) throws IOException;

    /**
     * 删除文件
     * @param filePath 文件存储路径
     * @return 删除成功返回true
     * @throws IOException IO异常
     */
    boolean delete(String filePath) throws IOException;

    /**
     * 检查文件是否存在
     * @param filePath 文件存储路径
     * @return 存在返回true
     */
    boolean exists(String filePath);

    /**
     * 获取文件完整访问URL
     * @param filePath 文件存储路径
     * @return 完整访问URL
     */
    String getUrl(String filePath);

    /**
     * 获取文件绝对路径
     * @param filePath 相对存储路径
     * @return 文件系统绝对路径
     */
    String getAbsolutePath(String filePath);

    /**
     * 获取存储类型标识
     * @return 存储类型（local、minio、oss等）
     */
    String getStorageType();
}
