package com.lw.graduation.api.controller.document;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentReviewDTO;
import com.lw.graduation.api.dto.document.DocumentUploadDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.common.response.Result;
import com.lw.graduation.common.enums.FileFormatType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文档管理控制器
 * 提供文档上传、下载、审核、查询等完整的API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/documents")
@Tag(name = "文档管理", description = "文档上传、下载、审核、查询等接口")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 分页查询文档列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询文档列表")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<IPage<DocumentVO>> getDocumentPage(DocumentPageQueryDTO queryDTO) {
        // 不同角色看到不同的文档数据
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 根据ID获取文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取文档详情")
    public Result<DocumentVO> getDocumentById(@PathVariable Long id) {
        // 需要验证用户是否有权限查看该文档
        return Result.success(documentService.getDocumentById(id));
    }

    /**
     * 上传文档
     *
     * @param uploadDTO 上传参数
     * @return 上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文档")
    @SaCheckRole({"student"})
    public Result<DocumentVO> uploadDocument(@ModelAttribute DocumentUploadDTO uploadDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        DocumentVO documentVO = documentService.uploadDocument(uploadDTO, userId);
        return Result.success(documentVO);
    }

    /**
     * 下载文档
     *
     * @param id 文档 ID
     * @return 文件下载响应
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载文档")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
    
        try (InputStream inputStream = documentService.downloadDocument(id, userId)) {
            // 获取文档信息用于设置响应头
            DocumentVO document = documentService.getDocumentById(id);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }
    
            // 读取文件内容
            byte[] bytes = inputStream.readAllBytes();
    
            // 设置响应头
            String filename = URLEncoder.encode(document.getOriginalFilename(), StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(bytes.length);
    
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
    
        } catch (Exception e) {
            log.error("文档下载失败：{}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 预览文档
     *
     * @param id 文档 ID
     * @return 文件预览响应
     */
    @GetMapping("/{id}/preview")
    @Operation(summary = "预览文档")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public ResponseEntity<byte[]> previewDocument(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
    
        try (InputStream inputStream = documentService.downloadDocument(id, userId)) {
            // 获取文档信息用于设置响应头
            DocumentVO document = documentService.getDocumentById(id);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }
    
            // 读取文件内容
            byte[] bytes = inputStream.readAllBytes();
    
            // 设置响应头，使用 inline 以便浏览器预览
            String filename = URLEncoder.encode(document.getOriginalFilename(), StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            // 根据文件类型设置 Content-Type
            String contentType = getContentType(document.getOriginalFilename());
            headers.setContentType(MediaType.parseMediaType(contentType));
            // 使用 inline 让浏览器尝试预览而不是下载
            headers.setContentDispositionFormData("inline", filename);
            headers.setContentLength(bytes.length);
    
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
    
        } catch (Exception e) {
            log.error("文档预览失败：{}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据文件名获取 Content-Type（复用 FileFormatType 枚举）
     */
    private String getContentType(String filename) {
        if (filename == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        
        // 获取文件扩展名
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex >= filename.length() - 1) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        
        // 复用 FileFormatType 枚举获取 MIME 类型
        FileFormatType fileType = FileFormatType.getByExtension(extension);
        if (fileType != null) {
            // 根据文件类别返回对应的 MIME 类型
            return switch (fileType.getCategory()) {
                case IMAGE -> switch (extension) {
                    case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
                    case "png" -> MediaType.IMAGE_PNG_VALUE;
                    case "gif" -> MediaType.IMAGE_GIF_VALUE;
                    default -> MediaType.IMAGE_JPEG_VALUE;
                };
                case DOCUMENT -> switch (extension) {
                    case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
                    case "txt", "md" -> MediaType.TEXT_PLAIN_VALUE;
                    default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
                };
                default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
            };
        }
        
        // 特殊处理 CSV
        if ("csv".equals(extension)) {
            return "text/csv";
        }
        
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    /**
     * 审核文档
     *
     * @param reviewDTO 审核参数
     * @return 审核结果
     */
    @PostMapping("/review")
    @Operation(summary = "审核文档")
    @SaCheckRole("teacher")
    public Result<Void> reviewDocument(@Validated @RequestBody DocumentReviewDTO reviewDTO) {
        Long reviewerId = StpUtil.getLoginIdAsLong();
        documentService.reviewDocument(reviewDTO, reviewerId);
        return Result.success();
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文档")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<Void> deleteDocument(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        documentService.deleteDocument(id, userId);
        return Result.success();
    }

    /**
     * 获取当前用户的文档列表
     *
     * @param queryDTO 查询条件
     * @return 文档列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取当前用户文档列表")
    @SaCheckRole({"student"})
    public Result<IPage<DocumentVO>> getMyDocuments(DocumentPageQueryDTO queryDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        queryDTO.setUserId(userId);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 获取某用户的文档列表
     *
     * @param userId 用户ID
     * @param queryDTO 查询条件
     * @return 文档列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户文档列表")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<IPage<DocumentVO>> getDocumentsByUser(@PathVariable Long userId, DocumentPageQueryDTO queryDTO) {
        queryDTO.setUserId(userId);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 获取某题目的文档列表
     *
     * @param topicId 题目ID
     * @param queryDTO 查询条件
     * @return 文档列表
     */
    @GetMapping("/topic/{topicId}")
    @Operation(summary = "获取题目文档列表")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<IPage<DocumentVO>> getDocumentsByTopic(@PathVariable Long topicId, DocumentPageQueryDTO queryDTO) {
        // 需要验证用户是否有权限查看该题目的文档
        queryDTO.setTopicId(topicId);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 获取某种类型的文档列表
     *
     * @param fileType 文件类型
     * @param queryDTO 查询条件
     * @return 文档列表
     */
    @GetMapping("/type/{fileType}")
    @Operation(summary = "获取指定类型文档列表")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<IPage<DocumentVO>> getDocumentsByType(@PathVariable Integer fileType, DocumentPageQueryDTO queryDTO) {
        queryDTO.setFileType(fileType);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 获取某种审核状态的文档列表
     *
     * @param reviewStatus 审核状态
     * @param queryDTO 查询条件
     * @return 文档列表
     */
    @GetMapping("/status/{reviewStatus}")
    @Operation(summary = "获取指定状态文档列表")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<IPage<DocumentVO>> getDocumentsByStatus(@PathVariable Integer reviewStatus, DocumentPageQueryDTO queryDTO) {
        queryDTO.setReviewStatus(reviewStatus);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }
}