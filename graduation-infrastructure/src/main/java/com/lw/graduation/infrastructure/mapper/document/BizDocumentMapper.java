package com.lw.graduation.infrastructure.mapper.document;

import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;

/**
 * <p>
 * 文档表 Mapper 接口
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
public interface BizDocumentMapper extends MyBaseMapper<BizDocument> {
    // 继承MyBaseMapper的通用方法
    // selectDetailsWithRelations - 批量查询文档详情及关联信息
    // selectBatchWithOrder - 增强版批量查询
    // selectStatistics - 通用统计方法

    // 特定业务方法可在此添加
}
