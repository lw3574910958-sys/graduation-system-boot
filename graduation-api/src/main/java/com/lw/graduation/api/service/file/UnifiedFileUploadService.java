package com.lw.graduation.api.service.file;

import com.lw.graduation.api.vo.file.FileUploadResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 统一文件上传服务接口
 * 处理各种类型的文件上传业务逻辑
 *
 * @author lw
 */
public interface UnifiedFileUploadService {

    /**
     * 通用文件上传
     *
     * @param file 文件
     * @param category 文件分类
     * @return 上传结果
     * @throws IOException IO异常
     */
    FileUploadResultVO uploadFile(MultipartFile file, String category) throws IOException;

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @param userId 用户ID
     * @return 上传结果
     * @throws IOException IO异常
     */
    FileUploadResultVO uploadAvatar(MultipartFile file, Long userId) throws IOException;

    /**
     * 上传文档
     *
     * @param file 文档文件
     * @param topicId 题目ID
     * @param fileType 文件类型
     * @param userId 用户ID
     * @return 上传结果
     * @throws IOException IO异常
     */
    FileUploadResultVO uploadDocument(MultipartFile file, Long topicId, Integer fileType, Long userId) throws IOException;

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 删除结果
     * @throws IOException IO异常
     */
    boolean deleteFile(String filePath) throws IOException;
}