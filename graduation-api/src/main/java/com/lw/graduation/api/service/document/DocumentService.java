package com.lw.graduation.api.service.document;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.document.DocumentCreateDTO;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentUpdateDTO;
import com.lw.graduation.api.vo.document.DocumentVO;

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
     * 创建新文档
     *
     * @param createDTO 创建文档 DTO
     */
    void createDocument(DocumentCreateDTO createDTO);

    /**
     * 更新文档信息
     *
     * @param id        文档ID
     * @param updateDTO 更新文档 DTO
     */
    void updateDocument(Long id, DocumentUpdateDTO updateDTO);

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void deleteDocument(Long id);
}