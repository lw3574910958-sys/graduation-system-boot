package com.lw.graduation.document.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentReviewDTO;
import com.lw.graduation.api.dto.document.DocumentUploadDTO;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.auth.service.PermissionValidationService;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.IEnum;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.common.util.CollectionUtils;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.document.DocumentFileType;
import com.lw.graduation.domain.enums.grade.GradeType;
import com.lw.graduation.domain.enums.status.ReviewStatus;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import com.lw.graduation.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final BizTopicMapper bizTopicMapper;
    private final SysUserMapper sysUserMapper;
    private final CacheHelper cacheHelper;
    private final FileStorageService fileStorageService;
    private final DataPermissionUtil dataPermissionUtil;
    private final PermissionValidationService permissionValidationService;
    private final com.lw.graduation.api.service.grade.GradeService gradeService;
    private final com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper bizGradeMapper;

    /**
     * 为审核通过的文档自动创建评分记录
     * 
     * @param document 已审核通过的文档
     */
    private void createGradeRecordForApprovedDocument(BizDocument document) {
        try {
            log.info("为审核通过的文档自动创建评分记录：文档 ID={}, 学生 ID={}, 题目 ID={}, 文件类型={}", 
                    document.getId(), document.getUserId(), document.getTopicId(), document.getFileType());
            
            // 1. 根据 user_id 获取学生业务 ID
            BizStudent student = dataPermissionUtil.getStudentByUserId(document.getUserId());
            if (student == null) {
                log.warn("无法找到学生信息：userId={}", document.getUserId());
                return;
            }
            
            // 2. 根据文档类型确定成绩类型
            GradeType gradeType = getGradeTypeByFileType(document.getFileType());
            if (gradeType == null) {
                log.warn("不支持的成绩类型：fileType={}", document.getFileType());
                return;
            }
            
            // 3. 检查是否已存在对应类型的成绩记录
            LambdaQueryWrapper<BizGrade> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(BizGrade::getStudentId, student.getId())
                   .eq(BizGrade::getTopicId, document.getTopicId())
                   .eq(BizGrade::getGradeType, gradeType.getCode())
                   .eq(BizGrade::getIsDeleted, 0);
            
            if (bizGradeMapper.selectCount(existWrapper) > 0) {
                log.info("成绩记录已存在，跳过创建：studentId={}, topicId={}, gradeType={}", 
                        student.getId(), document.getTopicId(), gradeType.getDescription());
                return;
            }
            
            // 4. 创建成绩记录（只填充基本信息，分数和评语等教师后续录入）
            GradeInputDTO inputDTO = new GradeInputDTO();
            inputDTO.setStudentId(student.getId());
            inputDTO.setTopicId(document.getTopicId());
            inputDTO.setGradeType(gradeType.getCode());
            // 注意：不设置 score 和 comment，由教师在正式评分时录入
            // gradedAt 也会在教师第一次评分时自动设置
            
            // 5. 调用成绩服务录入（使用文档的审核人 ID）
            gradeService.inputGradeForAutoCreate(inputDTO, document.getReviewerId());
            
            log.info("评分记录创建成功：studentId={}, topicId={}, gradeType={}, graderId={}", 
                    student.getId(), document.getTopicId(), gradeType.getDescription(), document.getReviewerId());
            
            // 6. 如果是毕业论文，尝试自动计算综合成绩（新增功能）
            if (gradeType == GradeType.THESIS_GRADE) {
                log.info("毕业论文审核通过，尝试自动计算综合成绩：studentId={}, topicId={}", 
                        student.getId(), document.getTopicId());
                gradeService.tryAutoSaveCompositeGrade(student.getId(), document.getTopicId(), document.getReviewerId());
            }
        } catch (Exception e) {
            log.error("创建评分记录失败：documentId={}, error={}", document.getId(), e.getMessage(), e);
            // 不抛出异常，避免影响审核流程
        }
    }

    /**
     * 根据文件类型获取对应的成绩类型
     * 
     * @param fileType 文件类型代码
     * @return 成绩类型枚举
     */
    private GradeType getGradeTypeByFileType(Integer fileType) {
        if (fileType == null) {
            return null;
        }
        
        return switch (fileType) {
            case 0 -> GradeType.PROPOSAL_GRADE; // 开题报告
            case 1 -> GradeType.MIDTERM_GRADE;  // 中期报告
            case 2 -> GradeType.THESIS_GRADE;   // 毕业论文
            default -> null;
        };
    }

    /**
     * 根据用户类型添加权限过滤条件（使用通用方法）
     *
     * 角色策略：
     * - 学生：只能查看自己提交的文档（user_id = 当前登录用户 ID）
     * - 教师：可以查看自己学生（选择该教师发布的题目）提交的所有文档
     * - 院系管理员：可以查看本院系题目的学生提交的所有文档
     * - 系统管理员：可以查看所有学生提交的文档
     *
     * @param wrapper 查询条件包装器
     * @param queryDTO 查询参数
     */
    private void addPermissionFilter(LambdaQueryWrapper<BizDocument> wrapper, DocumentPageQueryDTO queryDTO) {
        // 数据权限过滤（核心逻辑）
        dataPermissionUtil.addCommonDataPermissionFilter(
            wrapper,
            // 学生角色：只能查看自己的文档（注意：这里 studentId 实际是 biz_student.id）
            // 但 biz_document.user_id 关联的是 sys_user.id，所以需要特殊处理
            studentBizId -> {
                // 获取当前登录用户的 sys_user.id
                Long currentUserId = StpUtil.getLoginIdAsLong();
                wrapper.eq(BizDocument::getUserId, currentUserId);
                log.debug("学生角色文档权限过滤：userId={}", currentUserId);
            },
            // 教师角色：可以查看自己学生（选择该教师发布的题目）提交的所有文档
            teacherId -> wrapper.in(BizDocument::getTopicId,
                bizTopicMapper.selectList(
                    new LambdaQueryWrapper<BizTopic>()
                        .eq(BizTopic::getTeacherId, teacherId)
                        .select(BizTopic::getId)
                ).stream().map(BizTopic::getId).toList()
            ),
            // 院系管理员角色：可以查看本院系题目的学生提交的所有文档
            departmentId -> wrapper.in(BizDocument::getTopicId,
                bizTopicMapper.selectList(
                    new LambdaQueryWrapper<BizTopic>()
                        .eq(BizTopic::getDepartmentId, departmentId)
                        .select(BizTopic::getId)
                    ).stream().map(BizTopic::getId).toList()
            )
        );
    }

    @Override
    public IPage<DocumentVO> getDocumentPage(DocumentPageQueryDTO queryDTO) {
        log.info("分页查询文档列表，当前页：{}，每页大小：{}", queryDTO.getCurrent(), queryDTO.getSize());

        // 1. 构建查询条件并添加数据权限过滤
        LambdaQueryWrapper<BizDocument> wrapper = new LambdaQueryWrapper<>();

        // 2. 添加通用数据权限过滤（核心修复）
        addPermissionFilter(wrapper, queryDTO);

        // 3. 其他查询条件
        wrapper.eq(queryDTO.getTopicId() != null, BizDocument::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getFileType() != null, BizDocument::getFileType, queryDTO.getFileType())
                .eq(queryDTO.getReviewStatus() != null, BizDocument::getReviewStatus, queryDTO.getReviewStatus())
                .eq(BizDocument::getIsDeleted, 0);

        // 关键词搜索
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(BizDocument::getOriginalFilename, queryDTO.getKeyword());
        }

        wrapper.orderByDesc(BizDocument::getUploadedAt);

        // 4. 执行分页查询
        IPage<BizDocument> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizDocument> documentPage = bizDocumentMapper.selectPage(page, wrapper);

        // 5. 转换为 VO 并批量填充关联信息（优化 N+1 查询）
        List<DocumentVO> voList = convertToDocumentVOListOptimized(documentPage.getRecords());

        // 6. 在内存中过滤 userName、studentNumber、reviewerName 和 reviewerWorkNumber（因为这些字段不在 BizDocument 实体中）
        if (StringUtils.hasText(queryDTO.getUserName())) {
            voList = voList.stream()
                    .filter(vo -> vo.getUserName() != null && vo.getUserName().contains(queryDTO.getUserName()))
                    .toList();
        }
        if (StringUtils.hasText(queryDTO.getStudentNumber())) {
            voList = voList.stream()
                    .filter(vo -> vo.getStudentNumber() != null && vo.getStudentNumber().contains(queryDTO.getStudentNumber()))
                    .toList();
        }
        if (StringUtils.hasText(queryDTO.getReviewerName())) {
            voList = voList.stream()
                    .filter(vo -> vo.getReviewerName() != null && vo.getReviewerName().contains(queryDTO.getReviewerName()))
                    .toList();
        }
        if (StringUtils.hasText(queryDTO.getReviewerWorkNumber())) {
            voList = voList.stream()
                    .filter(vo -> vo.getReviewerWorkNumber() != null && vo.getReviewerWorkNumber().contains(queryDTO.getReviewerWorkNumber()))
                    .toList();
        }

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
        DocumentFileType fileType =
            IEnum.getByCode(DocumentFileType.class, uploadDTO.getFileType());
        if (fileType == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "不支持的文件类型");
        }

        // 2. 验证文档上传顺序（开题→中期→毕业）
        validateDocumentUploadOrder(userId, uploadDTO.getTopicId(), fileType);

        // 3. 验证用户是否有权限上传该题目的文档
        permissionValidationService.validateDocumentUploadPermission(userId, uploadDTO.getTopicId());

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
        document.setReviewStatus(ReviewStatus.PENDING.getCode());
        document.setDescription(uploadDTO.getDescription());
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
        permissionValidationService.validateDocumentDownloadPermission(userId, document);

        // 3. 下载文件
        try {
            return fileStorageService.download(document.getStoredPath());
        } catch (Exception e) {
            log.error("文档下载失败：{}", documentId, e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档下载失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewDocument(DocumentReviewDTO reviewDTO, Long reviewerId) {
        log.info("审核员 {} 审核文档：{}, 结果：{}", reviewerId, reviewDTO.getDocumentId(), reviewDTO.getReviewStatus());

        // 1. 获取文档信息
        BizDocument document = getById(reviewDTO.getDocumentId());
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 2. 验证审核权限（只有教师可以审核自己指导学生的文档）
        String reviewerRole = sysUserMapper.selectById(reviewerId).getUserType();
        if (!permissionValidationService.canAccessDocumentData(reviewerId, reviewDTO.getDocumentId(), reviewerRole)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权审核该文档");
        }

        // 3. 验证审核状态
        ReviewStatus reviewStatus = IEnum.getByCode(ReviewStatus.class, reviewDTO.getReviewStatus());
        if (reviewStatus == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "无效的审核状态");
        }

        // 4. 验证文档当前状态
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

        // 5. 更新审核信息
        document.setReviewStatus(reviewDTO.getReviewStatus());
        document.setReviewerId(reviewerId);
        document.setReviewedAt(LocalDateTime.now());
        document.setFeedback(reviewDTO.getFeedback());

        boolean updated = updateById(document);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档审核失败");
        }

        // 6. 清除缓存
        clearDocumentCache(document.getId());

        // 7. 如果审核通过且为教学环节文档（开题/中期/论文），自动创建评分记录（新增功能）
        if (reviewStatus == ReviewStatus.APPROVED && document.getFileType() != null) {
            createGradeRecordForApprovedDocument(document);
        }

        log.info("文档审核成功，ID: {}, 结果：{}", document.getId(), reviewStatus.getDescription());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id, Long userId) {
        log.info("用户 {} 删除文档：{}", userId, id);

        // 1. 获取文档信息
        BizDocument document = getById(id);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 2. 验证删除权限
        permissionValidationService.validateDocumentDeletePermission(id, userId, document);

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


    /**
     * 删除存储的文件
     */
    private void deleteStoredFile(String filePath) {
        try {
            fileStorageService.delete(filePath);
            log.debug("文件删除成功：{}", filePath);
        } catch (Exception e) {
            log.error("文件删除失败：{}", filePath, e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件删除失败");
        }
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
        ReviewStatus reviewStatus = IEnum.getByCode(ReviewStatus.class, document.getReviewStatus());
        if (reviewStatus != null) {
            vo.setReviewStatusDesc(reviewStatus.getDescription());
        }

        // 填充用户信息
        if (document.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(document.getUserId());
            if (user != null) {
                vo.setUserName(user.getRealName());
                // 查询学生的学号（只有学生能上传文档）
                if ("student".equals(user.getUserType())) {
                    // 使用 DataPermissionUtil 的方法，会自动添加逻辑删除条件
                    BizStudent student = dataPermissionUtil.getStudentByUserId(document.getUserId());
                    if (student != null) {
                        vo.setStudentNumber(student.getStudentId());
                        log.debug("查询到学号：{} for user_id: {}", student.getStudentId(), document.getUserId());
                    } else {
                        log.error("未查询到学号 for user_id: {}, 请检查 biz_student 表中是否有对应数据", document.getUserId());
                    }
                } else {
                    log.warn("用户类型不是学生：user_id: {}, type: {}", document.getUserId(), user.getUserType());
                }
            } else {
                log.error("未查询到用户信息：user_id: {}", document.getUserId());
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
        // 使用工具方法处理空集合检查和ID提取
        List<Long> documentIds = extractIdsFromDocuments(documents);
        if (documentIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询关联信息
        List<Map<String, Object>> documentDetails = bizDocumentMapper.selectDetailsWithRelations(documentIds);

        // 构建ID到详情的映射
        Map<Long, Map<String, Object>> detailsMap = documentDetails.stream()
                .collect(java.util.stream.Collectors.toMap(
                        detail -> ((Number) detail.get("id")).longValue(),
                        detail -> detail,
                        (existing, replacement) -> existing
                ));

        // 转换为 VO 列表
        return documents.stream().map(document -> {
            // 使用 BeanMapperUtil 复用属性拷贝功能
            DocumentVO vo = BeanMapperUtil.copyProperties(document, DocumentVO.class);

            // 填充扩展信息
            vo.setFileSizeDisplay(document.getFileSizeDisplay());
            vo.setFileExtension(document.getFileExtension());

            // 填充文件类型描述
            DocumentFileType fileType =
                        IEnum.getByCode(DocumentFileType.class, document.getFileType());
            if (fileType != null) {
                vo.setFileTypeDesc(fileType.getDescription());
            }

            // 填充审核状态描述
            ReviewStatus reviewStatus = IEnum.getByCode(ReviewStatus.class, document.getReviewStatus());
            if (reviewStatus != null) {
                vo.setReviewStatusDesc(reviewStatus.getDescription());
            }

            // 从批量查询结果中获取关联信息
            Map<String, Object> detail = detailsMap.get(document.getId());
            if (detail != null) {
                vo.setUserName((String) detail.get("user_name"));
                vo.setStudentNumber((String) detail.get("student_number"));
                vo.setTopicTitle((String) detail.get("topic_title"));
                vo.setReviewerName((String) detail.get("reviewer_name"));
                vo.setReviewerWorkNumber((String) detail.get("reviewer_work_number"));
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
            default -> "未知类型";
        };
    }

    /**
     * 从文档列表中提取 ID 列表（处理空值检查）
     * @param documents 文档列表
     * @return ID列表，如果输入为空则返回空列表
     */
    private List<Long> extractIdsFromDocuments(List<BizDocument> documents) {
        return CollectionUtils.extractIds(documents, BizDocument::getId);
    }

    /**
     * 清除文档相关缓存
     */
    private void clearDocumentCache(Long documentId) {
        String cacheKey = CacheConstants.KeyPrefix.DOCUMENT_INFO + documentId;
        cacheHelper.evictCache(cacheKey);
    }

    /**
     * 验证文档上传顺序（开题→中期→毕业）
     *
     * @param userId 用户 ID
     * @param topicId 题目 ID
     * @param fileType 要上传的文档类型
     * @throws BusinessException 如果前置文档未审核通过，抛出异常
     */
    private void validateDocumentUploadOrder(Long userId, Long topicId,
            DocumentFileType fileType) {
        // 仅对中期报告和毕业论文进行顺序验证
        if (fileType == DocumentFileType.MIDTERM) {
            // 上传中期报告前，必须先上传并审核通过开题报告
            LambdaQueryWrapper<BizDocument> proposalWrapper = new LambdaQueryWrapper<>();
            proposalWrapper.eq(BizDocument::getUserId, userId)
                    .eq(BizDocument::getTopicId, topicId)
                    .eq(BizDocument::getFileType, DocumentFileType.PROPOSAL.getCode())
                    .eq(BizDocument::getReviewStatus, ReviewStatus.APPROVED.getCode())
                    .eq(BizDocument::getIsDeleted, 0);

            if (count(proposalWrapper) == 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(),
                    "请先上传开题报告并等待审核通过后再上传中期报告");
            }
        } else if (fileType == DocumentFileType.THESIS) {
            // 上传毕业论文前，必须先上传并审核通过中期报告
            LambdaQueryWrapper<BizDocument> midtermWrapper = new LambdaQueryWrapper<>();
            midtermWrapper.eq(BizDocument::getUserId, userId)
                    .eq(BizDocument::getTopicId, topicId)
                    .eq(BizDocument::getFileType, DocumentFileType.MIDTERM.getCode())
                    .eq(BizDocument::getReviewStatus, ReviewStatus.APPROVED.getCode())
                    .eq(BizDocument::getIsDeleted, 0);

            if (count(midtermWrapper) == 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(),
                    "请先上传中期报告并等待审核通过后再上传毕业论文");
            }
        }
    }

    /**
     * 学生重新上传文档（驳回后）
     *
     * @param originalDocumentId 原文档 ID
     * @param uploadDTO 上传参数
     * @param userId 用户 ID
     * @return 重新上传后的文档 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentVO reuploadDocument(Long originalDocumentId, DocumentUploadDTO uploadDTO, Long userId) {
        log.info("学生用户 [{}] 重新上传文档，原文档 ID: {}", userId, originalDocumentId);

        // 1. 获取原文档信息
        BizDocument originalDocument = getById(originalDocumentId);
        if (originalDocument == null || originalDocument.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "原文档不存在");
        }

        // 2. 验证权限（只能重新上传自己的文档）
        if (!originalDocument.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权重新上传他人文档");
        }

        // 3. 验证文档状态（只能重新上传被驳回的文档）
        ReviewStatus reviewStatus = IEnum.getByCode(ReviewStatus.class, originalDocument.getReviewStatus());
        if (reviewStatus == null || reviewStatus != ReviewStatus.REJECTED) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只能重新上传被驳回的文档");
        }

        // 4. 验证文件类型是否一致
        if (!originalDocument.getFileType().equals(uploadDTO.getFileType())) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "重新上传的文件类型必须与原文档一致");
        }

        // 5. 上传新文件到存储服务
        DocumentFileType fileType =
            IEnum.getByCode(DocumentFileType.class, uploadDTO.getFileType());
        String folder = "documents/" + (fileType != null ? fileType.name().toLowerCase() : "other");
        String storedPath;
        try {
            storedPath = fileStorageService.store(uploadDTO.getFile(), folder);
        } catch (Exception e) {
            log.error("文件存储失败", e);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件上传失败");
        }

        // 6. 创建新的文档记录（替换原文档）
        BizDocument newDocument = new BizDocument();
        newDocument.setUserId(userId);
        newDocument.setTopicId(originalDocument.getTopicId());
        newDocument.setFileType(uploadDTO.getFileType());
        newDocument.setOriginalFilename(uploadDTO.getFile().getOriginalFilename());
        newDocument.setStoredPath(storedPath);
        newDocument.setFileSize(uploadDTO.getFile().getSize());
        newDocument.setReviewStatus(ReviewStatus.PENDING.getCode()); // 重新设置为待审核状态
        newDocument.setDescription(uploadDTO.getDescription());
        newDocument.setUploadedAt(LocalDateTime.now());

        boolean saved = save(newDocument);
        if (!saved) {
            // 上传失败时删除已存储的文件
            deleteStoredFile(storedPath);
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档重新上传失败");
        }

        // 7. 逻辑删除原有的文档记录
        boolean removed = removeById(originalDocumentId);
        if (!removed) {
            log.warn("逻辑删除原文档记录失败，原文档 ID: {}", originalDocumentId);
        }

        // 8. 清除相关缓存
        clearDocumentCache(originalDocumentId);
        clearDocumentCache(newDocument.getId());

        log.info("文档重新上传成功，新 ID: {}", newDocument.getId());
        return convertToDocumentVO(newDocument);
    }

    /**
     * 学生撤销文档申请（待审核状态）
     *
     * @param documentId 文档 ID
     * @param userId 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDocument(Long documentId, Long userId) {
        log.info("学生用户 [{}] 撤销文档申请，文档 ID: {}", userId, documentId);

        // 1. 获取文档信息
        BizDocument document = getById(documentId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 2. 验证撤销权限（只能撤销自己的文档）
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权撤销他人文档");
        }

        // 3. 验证文档状态（只能撤销待审核状态的文档）
        ReviewStatus reviewStatus = IEnum.getByCode(ReviewStatus.class, document.getReviewStatus());
        if (reviewStatus == null || reviewStatus != ReviewStatus.PENDING) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只能撤销待审核状态的文档");
        }

        // 4. 逻辑删除数据库记录（不删除文件存储，因为学生可能需要重新上传）
        boolean removed = removeById(documentId);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文档撤销失败");
        }

        // 5. 清除缓存
        clearDocumentCache(documentId);

        log.info("文档撤销成功，ID: {}", documentId);
    }
}