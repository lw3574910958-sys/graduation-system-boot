package com.lw.graduation.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentReviewDTO;
import com.lw.graduation.api.dto.document.DocumentUploadDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.common.enums.FileType;
import com.lw.graduation.domain.enums.status.ReviewStatus;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import com.lw.graduation.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档服务实现类
 * 实现文档管理模块的核心业务逻辑，调用基础设施层的文件存储服务
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl extends ServiceImpl<BizDocumentMapper, BizDocument> implements DocumentService {

    private final BizDocumentMapper bizDocumentMapper;
    private final BizSelectionMapper bizSelectionMapper;
    private final BizTopicMapper bizTopicMapper;
    private final SysUserMapper sysUserMapper;
    private final CacheHelper cacheHelper;
    private final FileStorageService fileStorageService;

    @Override
    public IPage<DocumentVO> getDocumentPage(DocumentPageQueryDTO queryDTO) {
        log.info("分页查询文档列表，当前页: {}，每页大小: {}，用户ID: {}，题目ID: {}", 
                queryDTO.getCurrent(), queryDTO.getSize(), queryDTO.getUserId(), queryDTO.getTopicId());

        // 1. 构建查询条件
        LambdaQueryWrapper<BizDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getUserId() != null, BizDocument::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getTopicId() != null, BizDocument::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getFileType() != null, BizDocument::getFileType, queryDTO.getFileType())
                .eq(queryDTO.getReviewStatus() != null, BizDocument::getReviewStatus, queryDTO.getReviewStatus())
                .eq(BizDocument::getIsDeleted, 0);

        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(BizDocument::getOriginalFilename, queryDTO.getKeyword());
        }

        wrapper.orderByDesc(BizDocument::getUploadedAt);

        // 2. 执行分页查询
        IPage<BizDocument> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizDocument> documentPage = bizDocumentMapper.selectPage(page, wrapper);

        // 3. 转换为VO并批量填充关联信息（优化N+1查询）
        List<DocumentVO> voList = convertToDocumentVOListOptimized(documentPage.getRecords());
        IPage<DocumentVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(voList);
        voPage.setTotal(documentPage.getTotal());

        return voPage;
    }

    @Override
    public DocumentVO getDocumentById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.DOCUMENT_INFO + id;

        return cacheHelper.getFromCache(cacheKey, DocumentVO.class, () -> {
            BizDocument document = bizDocumentMapper.selectById(id);
            if (document == null || document.getIsDeleted() == 1) {
                return null;
            }
            return convertToDocumentVO(document);
        }, CacheConstants.ExpireTime.WARM_DATA_EXPIRE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO uploadDocument(DocumentUploadDTO uploadDTO, Long userId) {
        log.info("用户[{}] 上传文档，文件名: {}，类型: {}，题目ID: {}", 
                userId, uploadDTO.getFile().getOriginalFilename(), uploadDTO.getFileType(), uploadDTO.getTopicId());

        // 1. 验证文件类型
        com.lw.graduation.domain.enums.document.FileType fileType = 
            com.lw.graduation.domain.enums.document.FileType.getByValue(uploadDTO.getFileType());
        if (fileType == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "不支持的文件类型");
        }

        // 2. 验证用户是否有权限上传该题目的文档
        validateUploadPermission(userId, uploadDTO.getTopicId());

        // 3. 检查是否已存在相同类型的文档
        LambdaQueryWrapper<BizDocument> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizDocument::getUserId, userId)
                .eq(BizDocument::getTopicId, uploadDTO.getTopicId())
                .eq(BizDocument::getFileType, uploadDTO.getFileType())
                .eq(BizDocument::getIsDeleted, 0);

        if (count(existWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该类型文档已存在，请先删除原文件");
        }

        // 4. 上传文件到存储服务
        String folder = "documents/" + fileType.name().toLowerCase();
        String storedPath;
        try {
            storedPath = fileStorageService.store(uploadDTO.getFile(), folder);
        } catch (Exception e) {
            log.error("文件存储失败", e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件上传失败");
        }

        // 5. 创建文档记录
        BizDocument document = new BizDocument();
        document.setUserId(userId);
        document.setTopicId(uploadDTO.getTopicId());
        document.setFileType(uploadDTO.getFileType());
        document.setOriginalFilename(uploadDTO.getFile().getOriginalFilename());
        document.setStoredPath(storedPath);
        document.setFileSize(uploadDTO.getFile().getSize());
        document.setReviewStatus(ReviewStatus.PENDING.getValue());
        document.setUploadedAt(LocalDateTime.now());

        boolean saved = save(document);
        if (!saved) {
            // 上传失败时删除已存储的文件
            deleteStoredFile(storedPath);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档上传失败");
        }

        // 6. 清除相关缓存
        clearDocumentCache(document.getId());

        log.info("文档上传成功，ID: {}", document.getId());
        return convertToDocumentVO(document);
    }


    @Override
    public InputStream downloadDocument(Long documentId, Long userId) {
        log.info("用户 {} 下载文档: {}", userId, documentId);

        // 1. 获取文档信息
        BizDocument document = getById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 2. 验证下载权限
        validateDownloadPermission(userId, document);

        // 3. 下载文件
        try {
            // 注意：这里需要根据具体的文件存储实现来获取InputStream
            // 当前假设LocalFileStorageServiceImpl提供了相应的方法
            // 实际使用时可能需要扩展FileStorageService接口
            throw new UnsupportedOperationException("当前文件存储实现暂不支持直接返回InputStream");
        } catch (Exception e) {
            log.error("文档下载失败: {}", documentId, e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档下载失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewDocument(DocumentReviewDTO reviewDTO, Long reviewerId) {
        log.info("审核员 {} 审核文档: {}, 结果: {}", reviewerId, reviewDTO.getDocumentId(), reviewDTO.getReviewStatus());

        // 1. 获取文档信息
        BizDocument document = getById(reviewDTO.getDocumentId());
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 2. 验证审核状态
        ReviewStatus reviewStatus = ReviewStatus.getByValue(reviewDTO.getReviewStatus());
        if (reviewStatus == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "无效的审核状态");
        }

        // 3. 验证文档当前状态
        if (document.isApproved()) {
            if (reviewStatus.isFinalStatus()) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文档已通过审核，无需重复审核");
            }
        } else if (document.isRejected()) {
            if (reviewStatus.isFinalStatus()) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文档已被驳回，不能再次审核");
            }
        } else if (document.isPendingReview()) {
            // 待审核状态可以进行任何审核操作
            log.debug("文档处于待审核状态，可以进行审核操作");
        }

        // 3. 更新审核信息
        document.setReviewStatus(reviewDTO.getReviewStatus());
        document.setReviewerId(reviewerId);
        document.setReviewedAt(LocalDateTime.now());
        document.setFeedback(reviewDTO.getFeedback());

        boolean updated = updateById(document);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档审核失败");
        }

        // 4. 清除缓存
        clearDocumentCache(document.getId());

        log.info("文档审核完成，ID: {}", document.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id, Long userId) {
        log.info("用户 {} 删除文档: {}", userId, id);

        // 1. 获取文档信息
        BizDocument document = getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 2. 验证删除权限
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权删除他人文档");
        }

        // 3. 验证文档状态（已通过审核的文档不能删除）
        if (document.isApproved()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已通过审核的文档不能删除");
        }

        // 3. 删除文件存储
        try {
            deleteStoredFile(document.getStoredPath());
        } catch (Exception e) {
            log.warn("文件删除失败，但继续删除数据库记录: {}", document.getStoredPath(), e);
        }

        // 4. 逻辑删除数据库记录
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档删除失败");
        }

        // 5. 清除缓存
        clearDocumentCache(id);

        log.info("文档删除成功，ID: {}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO resubmitDocument(Long documentId, Long userId, MultipartFile newFile) {
        log.info("用户 {} 重新提交文档: {}", userId, documentId);
        
        // 1. 获取文档信息
        BizDocument document = getById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }
        
        // 2. 验证权限和状态
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权限重新提交此文档");
        }
        
        ReviewStatus currentStatus = ReviewStatus.getByValue(document.getReviewStatus());
        if (currentStatus == null || !currentStatus.canResubmit()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文档状态不允许重新提交");
        }
        
        // 3. 验证新文件
        if (newFile == null || newFile.isEmpty()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "新文件不能为空");
        }
        
        String originalFilename = newFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 验证文件类型
        FileType.ValidationResult result = FileType.validate(extension, newFile.getSize());
        if (!result.isValid()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), result.getMessage());
        }
        
        // 4. 上传新文件
        String category = "document/topic_" + document.getTopicId() + "/" + document.getFileType();
        String newStoredPath;
        try {
            newStoredPath = fileStorageService.store(newFile, category);
        } catch (Exception e) {
            log.error("文件上传失败: {}", documentId, e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件上传失败");
        }
        
        // 5. 更新文档信息
        document.setOriginalFilename(originalFilename);
        document.setStoredPath(newStoredPath);
        document.setFileSize(newFile.getSize());
        document.setReviewStatus(ReviewStatus.PENDING.getValue()); // 重置为待审核状态
        document.setReviewedAt(null);
        document.setReviewerId(null);
        document.setFeedback(null);
        document.setUploadedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        
        boolean updated = updateById(document);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档重新提交失败");
        }
        
        // 6. 清除缓存
        clearDocumentCache(documentId);
        
        // 7. 转换为VO并返回
        return convertToDocumentVO(document);
    }

    /**
     * 删除存储的文件（需要扩展FileStorageService接口）
     */
    private void deleteStoredFile(String filePath) {
        // TODO: 需要在FileStorageService接口中添加delete方法
        // 或者创建一个专门的文件管理服务
        log.warn("文件删除功能待实现，路径: {}", filePath);
    }

    /**
     * 验证上传权限
     */
    private void validateUploadPermission(Long userId, Long topicId) {
        // 检查用户是否选择了该题目
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, userId)
                       .eq(BizSelection::getTopicId, topicId)
                       .eq(BizSelection::getStatus, 1); // 已确认状态

        if (bizSelectionMapper.selectCount(selectionWrapper) == 0) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权上传该题目的文档");
        }
    }

    /**
     * 验证下载权限
     */
    private void validateDownloadPermission(Long userId, BizDocument document) {
        // 文档所有者可以下载
        if (document.getUserId().equals(userId)) {
            return;
        }

        // 指导教师可以下载
        BizTopic topic = bizTopicMapper.selectById(document.getTopicId());
        if (topic != null && topic.getTeacherId().equals(userId)) {
            return;
        }

        // 管理员可以下载
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null && "admin".equals(user.getUsername())) {
            return;
        }

        throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权下载该文档");
    }

    /**
     * 转换文档实体为VO
     */
    private DocumentVO convertToDocumentVO(BizDocument document) {
        DocumentVO vo = BeanMapperUtil.copyProperties(document, DocumentVO.class);

        // 填充扩展信息
        vo.setFileSizeDisplay(document.getFileSizeDisplay());
        vo.setFileExtension(document.getFileExtension());

        // 填充文件类型描述
        String fileTypeDesc = getFileTypeDescription(document.getFileType());
        if (fileTypeDesc != null) {
            vo.setFileTypeDesc(fileTypeDesc);
        }

        // 填充审核状态描述
        ReviewStatus reviewStatus = ReviewStatus.getByValue(document.getReviewStatus());
        if (reviewStatus != null) {
            vo.setReviewStatusDesc(reviewStatus.getDescription());
        }

        // 填充用户信息
        if (document.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(document.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
            }
        }

        // 填充题目信息
        if (document.getTopicId() != null) {
            BizTopic topic = bizTopicMapper.selectById(document.getTopicId());
            if (topic != null) {
                vo.setTopicTitle(topic.getTitle());
            }
        }

        // 填充审核人信息
        if (document.getReviewerId() != null) {
            SysUser reviewer = sysUserMapper.selectById(document.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getRealName());
            }
        }

        return vo;
    }

    /**
     * 批量转换文档实体为VO（优化N+1查询）
     * 通过批量查询减少数据库访问次数
     */
    private List<DocumentVO> convertToDocumentVOListOptimized(List<BizDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        // 提取所有需要查询的ID
        List<Long> documentIds = documents.stream()
                .map(BizDocument::getId)
                .toList();

        // 批量查询关联信息
        List<Map<String, Object>> documentDetails = bizDocumentMapper.selectDetailsWithRelations(documentIds);

        // 构建ID到详情的映射
        Map<Long, Map<String, Object>> detailsMap = documentDetails.stream()
                .collect(java.util.stream.Collectors.toMap(
                        detail -> ((Number) detail.get("id")).longValue(),
                        detail -> detail,
                        (existing, replacement) -> existing
                ));

        // 转换为VO列表
        return documents.stream().map(document -> {
            DocumentVO vo = new DocumentVO();
            vo.setId(document.getId());
            vo.setUserId(document.getUserId());
            vo.setTopicId(document.getTopicId());
            vo.setFileType(document.getFileType());
            vo.setOriginalFilename(document.getOriginalFilename());
            vo.setFileSize(document.getFileSize());
            vo.setReviewStatus(document.getReviewStatus());
            vo.setReviewedAt(document.getReviewedAt());
            vo.setReviewerId(document.getReviewerId());
            vo.setFeedback(document.getFeedback());
            vo.setUploadedAt(document.getUploadedAt());
            vo.setCreatedAt(document.getCreatedAt());
            vo.setUpdatedAt(document.getUpdatedAt());

            // 填充扩展信息
            vo.setFileSizeDisplay(document.getFileSizeDisplay());
            vo.setFileExtension(document.getFileExtension());

            // 填充文件类型描述
            com.lw.graduation.domain.enums.document.FileType fileType = 
                com.lw.graduation.domain.enums.document.FileType.getByValue(document.getFileType());
            if (fileType != null) {
                vo.setFileTypeDesc(fileType.getDescription());
            }

            // 填充审核状态描述
            ReviewStatus reviewStatus = ReviewStatus.getByValue(document.getReviewStatus());
            if (reviewStatus != null) {
                vo.setReviewStatusDesc(reviewStatus.getDescription());
            }

            // 从批量查询结果中获取关联信息
            Map<String, Object> detail = detailsMap.get(document.getId());
            if (detail != null) {
                vo.setUserName((String) detail.get("user_name"));
                vo.setTopicTitle((String) detail.get("topic_title"));
                vo.setReviewerName((String) detail.get("reviewer_name"));
            }

            return vo;
        }).toList();
    }

    /**
     * 获取文件类型描述
     */
    private String getFileTypeDescription(Integer fileTypeValue) {
        if (fileTypeValue == null) {
            return null;
        }
        
        return switch (fileTypeValue) {
            case 0 -> "开题报告";
            case 1 -> "中期报告";
            case 2 -> "毕业论文";
            case 3 -> "外文翻译";
            case 4 -> "其他文档";
            default -> "未知类型";
        };
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 清除文档相关缓存
     */
    private void clearDocumentCache(Long documentId) {
        String cacheKey = CacheConstants.KeyPrefix.DOCUMENT_INFO + documentId;
        cacheHelper.evictCache(cacheKey);
    }
}