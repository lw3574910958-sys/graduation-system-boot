package com.lw.graduation.api.controller.document;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.document.DocumentCreateDTO;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentUpdateDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档管理控制器
 * 提供文档信息的增删改查、分页查询、详情获取等API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/documents")
@Tag(name = "文档管理", description = "文档信息的增删改查、分页查询、详情获取、审核等接口")
@RequiredArgsConstructor
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
     * @param createDTO 创建参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "上传文档")
    @SaCheckRole({"student", "teacher"}) // 学生和教师都可以上传文档
    public Result<Void> createDocument(@Validated @RequestBody DocumentCreateDTO createDTO) {
        documentService.createDocument(createDTO);
        return Result.success();
    }

    /**
     * 更新文档信息
     *
     * @param id 文档ID
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文档信息")
    @SaCheckRole({"student", "teacher"}) // 学生和教师都可以更新自己的文档
    public Result<Void> updateDocument(@PathVariable Long id, @Validated @RequestBody DocumentUpdateDTO updateDTO) {
        documentService.updateDocument(id, updateDTO);
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
    @SaCheckRole({"student", "teacher"}) // 学生和教师都可以删除自己的文档
    public Result<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    /**
     * 审核文档（教师操作）
     *
     * @param id 文档ID
     * @param updateDTO 审核参数
     * @return 审核结果
     */
    @PutMapping("/{id}/review")
    @Operation(summary = "审核文档")
    @SaCheckRole("teacher") // 仅教师可审核文档
    public Result<Void> reviewDocument(@PathVariable Long id, @Validated @RequestBody DocumentUpdateDTO updateDTO) {
        documentService.updateDocument(id, updateDTO);
        return Result.success();
    }

    /**
     * 获取某用户的文档列表
     *
     * @param userId 用户ID
     * @return 文档列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户文档列表")
    public Result<IPage<DocumentVO>> getDocumentsByUser(@PathVariable Long userId, DocumentPageQueryDTO queryDTO) {
        queryDTO.setUserId(userId);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 获取某选题的文档列表
     *
     * @param topicId 选题ID
     * @return 文档列表
     */
    @GetMapping("/topic/{topicId}")
    @Operation(summary = "获取选题文档列表")
    public Result<IPage<DocumentVO>> getDocumentsByTopic(@PathVariable Long topicId, DocumentPageQueryDTO queryDTO) {
        queryDTO.setTopicId(topicId);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }

    /**
     * 获取某种类型的文档列表
     *
     * @param fileType 文件类型
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
     * @return 文档列表
     */
    @GetMapping("/status/{reviewStatus}")
    @Operation(summary = "获取指定状态文档列表")
    public Result<IPage<DocumentVO>> getDocumentsByStatus(@PathVariable Integer reviewStatus, DocumentPageQueryDTO queryDTO) {
        queryDTO.setReviewStatus(reviewStatus);
        return Result.success(documentService.getDocumentPage(queryDTO));
    }
}