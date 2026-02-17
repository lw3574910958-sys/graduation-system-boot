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
        try {
            FileUploadResultVO result = unifiedFileUploadService.uploadFile(file, category);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
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
    public Result<FileUploadResultVO> uploadAvatar(
            @Parameter(description = "上传的头像文件") @RequestParam("file") MultipartFile file) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            FileUploadResultVO result = unifiedFileUploadService.uploadAvatar(file, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}