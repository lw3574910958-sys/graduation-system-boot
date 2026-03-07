package com.lw.graduation.api.controller.file;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.api.service.file.UnifiedFileUploadService;
import com.lw.graduation.api.vo.file.FileUploadResultVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 文件上传控制器
 * 提供文件上传相关 API 端点，包括通用文件上传和头像上传功能。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final UnifiedFileUploadService unifiedFileUploadService;

    /**
     * 通用文件上传接口
     *
     * @param file 上传的文件
     * @param category 文件分类
     * @return 上传结果
     */
    @PostMapping("/file")
    @SaCheckLogin // 需要登录才能上传
    @Operation(summary = "上传文件")
    public Result<FileUploadResultVO> uploadFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件分类") @RequestParam(required = false, defaultValue = "general") String category) {
        // 需要进一步验证用户是否有权限上传该类型文件
        Long userId = StpUtil.getLoginIdAsLong();
    
        try {
            FileUploadResultVO result = unifiedFileUploadService.uploadFile(file, category);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
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
    public Result<FileUploadResultVO> uploadAvatar(
            @Parameter(description = "上传的头像文件") @RequestParam("file") MultipartFile file) {
        // 验证用户只能上传自己的头像
        Long userId = StpUtil.getLoginIdAsLong();
    
        try {
            // 1. 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 2. 检查文件大小（头像限制为 2MB）
            long maxSize = 2 * 1024 * 1024; // 2MB
            if (file.getSize() > maxSize) {
                return Result.error("头像文件大小不能超过 2MB");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return Result.error("文件名无效");
            }

            // 3. 安全性：防止路径遍历
            if (!isValidFileName(originalFilename)) {
                return Result.error("非法文件名");
            }

            // 4. 检查扩展名
            if (!isAllowedImageExtension(originalFilename)) {
                return Result.error("仅支持 jpg、jpeg、png、gif 格式");
            }

            // 5. 验证是否为真实图片
            if (!isValidImage(file)) {
                return Result.error("上传的不是有效图片文件");
            }

            // 6. 执行上传
            FileUploadResultVO result = unifiedFileUploadService.uploadAvatar(file, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    // ------------------- 工具方法 -------------------

    /**
     * 验证文件名是否合法
     */
    private boolean isValidFileName(String filename) {
        // 禁止 .. / \ 开头
        return !filename.contains("..") &&
                !filename.contains("/") &&
                !filename.contains("\\") &&
                !filename.startsWith(".");
    }

    /**
     * 检查是否为允许的图片扩展名
     */
    private boolean isAllowedImageExtension(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") ||
                lower.endsWith(".png") ||
                lower.endsWith(".gif");
    }

    /**
     * 验证是否为有效的图片文件
     */
    private boolean isValidImage(MultipartFile file) {
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            return img != null;
        } catch (IOException e) {
            return false;
        }
    }
}