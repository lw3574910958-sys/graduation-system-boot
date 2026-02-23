package com.lw.graduation.api.service.document;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentReviewDTO;
import com.lw.graduation.api.dto.document.DocumentUploadDTO;
import com.lw.graduation.api.vo.document.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文档服务接口
 * 定义文档管理模块的核心业务逻辑。
 *
 * @author lw
 */
public interface DocumentService {

    /**
     * 分页查询文档列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<DocumentVO> getDocumentPage(DocumentPageQueryDTO queryDTO);

    /**
     * 根据ID获取文档详情
     *
     * @param id 文档ID
     * @return 文档详情 VO
     */
    DocumentVO getDocumentById(Long id);

    /**
     * 上传文档
     *
     * @param uploadDTO 上传文档 DTO
     * @param userId 用户ID
     * @return 文档VO
     */
    DocumentVO uploadDocument(DocumentUploadDTO uploadDTO, Long userId);

    /**
     * 下载文档
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 文件输入流
     */
    InputStream downloadDocument(Long documentId, Long userId);

    /**
     * 审核文档
     *
     * @param reviewDTO 审核DTO
     * @param reviewerId 审核人ID
     */
    void reviewDocument(DocumentReviewDTO reviewDTO, Long reviewerId);

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @param userId 用户ID
     */
    void deleteDocument(Long id, Long userId);
    
    /**
     * 重新提交被驳回的文档
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param newFile 新文件
     * @return 更新后的文档VO
     */
    DocumentVO resubmitDocument(Long documentId, Long userId, MultipartFile newFile);
}