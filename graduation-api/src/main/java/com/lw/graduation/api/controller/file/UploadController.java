package com.lw.graduation.api.controller.file;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.lw.graduation.api.config.FileConfig;
import com.lw.graduation.common.response.Result;
import com.lw.graduation.infrastructure.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 提供文件上传相关API端点，包括通用文件上传和头像上传功能。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    /**
     * 通用文件上传接口
     *
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping("/file")
    @SaCheckLogin // 需要登录才能上传
    @Operation(summary = "上传文件")
    public Result<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 使用文件存储服务保存文件
            String storedPath = fileStorageService.store(file, FileConfig.UPLOAD_BASE_PATH);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("name", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("type", file.getContentType());
            result.put("storedPath", storedPath);
            result.put("url", "/files" + storedPath); // 提供访问URL

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像接口
     *
     * @param file 上传的头像文件
     * @return 上传结果
     */
    @PostMapping("/avatar")
    @SaCheckLogin // 需要登录才能上传头像
    @Operation(summary = "上传头像")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("头像文件不能为空");
        }

        // 验证文件类型（仅允许图片格式）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("仅支持图片格式文件");
        }
        
        // 额外验证：检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(extension)) {
                return Result.error("仅支持 JPG、JPEG、PNG、GIF 格式的图片");
            }
        }

        try {
            // 使用文件存储服务保存头像文件
            String storedPath = fileStorageService.store(file, FileConfig.UPLOAD_BASE_PATH + FileConfig.AVATAR_DIR);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("name", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("type", file.getContentType());
            result.put("storedPath", storedPath);
            result.put("url", "/files" + storedPath); // 提供访问URL

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}