package com.lw.graduation.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.document.DocumentCreateDTO;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentUpdateDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.FileType;
import com.lw.graduation.domain.enums.ReviewStatus;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文档服务实现类
 * 实现文档管理模块的核心业务逻辑。
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
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询文档列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<DocumentVO> getDocumentPage(DocumentPageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getUserId() != null, BizDocument::getUserId, queryDTO.getUserId())
                .eq(queryDTO.getTopicId() != null, BizDocument::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getFileType() != null, BizDocument::getFileType, queryDTO.getFileType())
                .eq(queryDTO.getReviewStatus() != null, BizDocument::getReviewStatus, queryDTO.getReviewStatus())
                .orderByDesc(BizDocument::getCreatedAt); // 按创建时间倒序

        // 2. 执行分页查询
        IPage<BizDocument> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizDocument> documentPage = bizDocumentMapper.selectPage(page, wrapper);

        // 3. 转换为VO
        IPage<DocumentVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(documentPage.getRecords().stream()
                .map(this::convertToDocumentVO)
                .collect(Collectors.toList()));
        voPage.setTotal(documentPage.getTotal());

        return voPage;
    }

    /**
     * 根据ID获取文档详情（带缓存穿透防护）
     *
     * @param id 文档ID
     * @return 文档详情
     */
    @Override
    public DocumentVO getDocumentById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.DOCUMENT_INFO + id;
        
        // 1. 查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (CacheConstants.CacheValue.NULL_MARKER.equals(cached)) {
                log.debug("缓存命中空值标记，文档不存在: " + id);
                return null;
            }
            return (DocumentVO) cached;
        }

        // 2. 缓存未命中，查数据库
        BizDocument document = bizDocumentMapper.selectById(id);
        if (document == null) {
            // 缓存空值防止穿透
            redisTemplate.opsForValue().set(
                cacheKey,
                CacheConstants.CacheValue.NULL_MARKER,
                CacheConstants.CacheValue.NULL_EXPIRE,
                TimeUnit.SECONDS
            );
            log.debug("文档不存在，缓存空值标记: " + cacheKey);
            return null;
        }

        // 3. 转换并缓存结果
        DocumentVO result = convertToDocumentVO(document);
        redisTemplate.opsForValue().set(
            cacheKey,
            result,
            CacheConstants.ExpireTime.DOCUMENT_INFO_EXPIRE,
            TimeUnit.SECONDS
        );
        log.debug("缓存文档信息: " + cacheKey);
        return result;
    }

    /**
     * 创建文档
     *
     * @param createDTO 创建参数
     */
    @Override
    @Transactional
    public void createDocument(DocumentCreateDTO createDTO) {
        // 1. 验证用户是否存在
        SysUser user = sysUserMapper.selectById(createDTO.getUserId());
        if (user == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "用户不存在");
        }

        // 2. 验证选题是否存在
        BizSelection selection = bizSelectionMapper.selectById(createDTO.getTopicId());
        if (selection == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "选题不存在");
        }

        // 3. 验证文件类型是否合法
        if (createDTO.getFileType() != null) {
            boolean validType = false;
            for (FileType type : FileType.values()) {
                if (type.getValue().equals(createDTO.getFileType())) {
                    validType = true;
                    break;
                }
            }
            if (!validType) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件类型不合法");
            }
        }

        // 4. 检查同一选题下是否已存在相同类型的文档
        LambdaQueryWrapper<BizDocument> duplicateWrapper = new LambdaQueryWrapper<>();
        duplicateWrapper.eq(BizDocument::getTopicId, createDTO.getTopicId())
                .eq(BizDocument::getFileType, createDTO.getFileType())
                .eq(BizDocument::getReviewStatus, ReviewStatus.APPROVED.getValue()); // 已通过审核的文档
        if (bizDocumentMapper.selectCount(duplicateWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该选题已存在相同类型的已审核文档");
        }

        // 5. 创建文档实体
        BizDocument document = new BizDocument();
        document.setUserId(createDTO.getUserId());
        document.setTopicId(createDTO.getTopicId());
        document.setFileType(createDTO.getFileType());
        document.setOriginalFilename(createDTO.getOriginalFilename());
        document.setStoredPath(createDTO.getStoredPath());
        document.setFileSize(createDTO.getFileSize());
        document.setReviewStatus(createDTO.getReviewStatus() != null ? createDTO.getReviewStatus() : 0); // 默认待审
        document.setUploadedAt(LocalDateTime.now());

        // 6. 插入数据库
        bizDocumentMapper.insert(document);
        
        // 7. 清除相关缓存
        clearUserDocumentsCache(createDTO.getUserId());
        clearTopicDocumentsCache(createDTO.getTopicId());
    }

    /**
     * 更新文档
     *
     * @param id 文档ID
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional
    public void updateDocument(Long id, DocumentUpdateDTO updateDTO) {
        // 1. 查询文档是否存在
        BizDocument existingDocument = bizDocumentMapper.selectById(id);
        if (existingDocument == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 如果更新审核状态，需要设置审核时间和审核人
        BizDocument updateDocument = new BizDocument();
        updateDocument.setId(id);
        updateDocument.setOriginalFilename(updateDTO.getOriginalFilename());
        updateDocument.setStoredPath(updateDTO.getStoredPath());
        updateDocument.setFileSize(updateDTO.getFileSize());
        
        if (updateDTO.getUserId() != null) {
            updateDocument.setUserId(updateDTO.getUserId());
        }
        if (updateDTO.getTopicId() != null) {
            updateDocument.setTopicId(updateDTO.getTopicId());
        }
        if (updateDTO.getFileType() != null) {
            updateDocument.setFileType(updateDTO.getFileType());
        }
        if (updateDTO.getReviewStatus() != null) {
            updateDocument.setReviewStatus(updateDTO.getReviewStatus());
            updateDocument.setReviewedAt(LocalDateTime.now());
            // TODO(lw): 这里应该从上下文中获取当前登录用户的ID作为审核人 @2024-12-31前完成
            // updateDocument.setReviewerId(currentUserId);
        }
        updateDocument.setUpdatedAt(LocalDateTime.now());

        // 3. 执行更新
        bizDocumentMapper.updateById(updateDocument);
        
        // 4. 清除缓存
        clearDocumentCache(id);
        if (updateDTO.getUserId() != null && !updateDTO.getUserId().equals(existingDocument.getUserId())) {
            clearUserDocumentsCache(existingDocument.getUserId());
            clearUserDocumentsCache(updateDTO.getUserId());
        }
        if (updateDTO.getTopicId() != null && !updateDTO.getTopicId().equals(existingDocument.getTopicId())) {
            clearTopicDocumentsCache(existingDocument.getTopicId());
            clearTopicDocumentsCache(updateDTO.getTopicId());
        }
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    @Override
    @Transactional
    public void deleteDocument(Long id) {
        // 1. 检查文档是否存在
        BizDocument document = bizDocumentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 已通过审核的文档不能直接删除
        if (document.getReviewStatus() != null && document.getReviewStatus() == ReviewStatus.APPROVED.getValue()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已通过审核的文档不能删除");
        }

        // 3. 执行删除（逻辑删除）
        bizDocumentMapper.deleteById(id);
        
        // 4. 清除缓存
        clearDocumentCache(id);
        clearUserDocumentsCache(document.getUserId());
        clearTopicDocumentsCache(document.getTopicId());
    }

    /**
     * 将BizDocument实体转换为DocumentVO
     *
     * @param document 文档实体
     * @return 文档VO
     */
    private DocumentVO convertToDocumentVO(BizDocument document) {
        DocumentVO vo = new DocumentVO();
        vo.setId(document.getId());
        vo.setUserId(document.getUserId());
        vo.setTopicId(document.getTopicId());
        vo.setFileType(document.getFileType());
        vo.setOriginalFilename(document.getOriginalFilename());
        vo.setStoredPath(document.getStoredPath());
        vo.setFileSize(document.getFileSize());
        vo.setReviewStatus(document.getReviewStatus());
        vo.setReviewedAt(document.getReviewedAt());
        vo.setReviewerId(document.getReviewerId());
        vo.setFeedback(document.getFeedback());
        vo.setUploadedAt(document.getUploadedAt());
        vo.setCreatedAt(document.getCreatedAt());
        vo.setUpdatedAt(document.getUpdatedAt());
        return vo;
    }

    /**
     * 清除单个文档缓存
     */
    private void clearDocumentCache(Long documentId) {
        if (documentId != null) {
            String cacheKey = CacheConstants.KeyPrefix.DOCUMENT_INFO + documentId;
            redisTemplate.delete(cacheKey);
            log.debug("清除文档缓存: " + cacheKey);
        }
    }

    /**
     * 清除用户相关文档缓存
     */
    private void clearUserDocumentsCache(Long userId) {
        if (userId != null) {
            // 可以扩展清除用户相关的文档列表缓存
            log.debug("清除用户文档相关缓存: " + userId);
        }
    }

    /**
     * 清除课题相关文档缓存
     */
    private void clearTopicDocumentsCache(Long topicId) {
        if (topicId != null) {
            // 可以扩展清除课题相关的文档列表缓存
            log.debug("清除课题文档相关缓存: " + topicId);
        }
    }
}