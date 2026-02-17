package com.lw.graduation.api.controller.document;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentReviewDTO;
import com.lw.graduation.api.dto.document.DocumentUploadDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Result<IPage<DocumentVO>> getDocumentPage(DocumentPageQueryDTO queryDTO) {
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
        return Result.success(documentService.getDocumentById(id));
    }

    /**
     * 上传文档
     *
     * @param topicId 题目ID
     * @param fileType 文件类型
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文档")
    @SaCheckRole({"student", "teacher"})
    public Result<DocumentVO> uploadDocument(
            @Parameter(description = "题目ID") @RequestParam Long topicId,
            @Parameter(description = "文件类型: 0-开题报告, 1-中期报告, 2-毕业论文, 3-外文翻译, 4-其他文档") @RequestParam Integer fileType,
            @Parameter(description = "上传的文件") @RequestParam MultipartFile file) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        
        DocumentUploadDTO uploadDTO = new DocumentUploadDTO();
        uploadDTO.setTopicId(topicId);
        uploadDTO.setFileType(fileType);
        uploadDTO.setFile(file);
        
        DocumentVO documentVO = documentService.uploadDocument(uploadDTO, userId);
        return Result.success(documentVO);
    }

    /**
     * 下载文档
     *
     * @param id 文档ID
     * @return 文件下载响应
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载文档")
    @SaCheckRole({"student", "teacher", "admin"})
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
            log.error("文档下载失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
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
    @SaCheckRole({"student", "teacher"})
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
    @SaCheckRole({"student", "teacher"})
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
    public Result<IPage<DocumentVO>> getDocumentsByTopic(@PathVariable Long topicId, DocumentPageQueryDTO queryDTO) {
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
    public Result<IPage<DocumentVO>> getDocumentsByStatus(@PathVariable Integer reviewStatus, DocumentPageQueryDTO queryDTO) {
        queryDTO.setReviewStatus(reviewStatus);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }
}