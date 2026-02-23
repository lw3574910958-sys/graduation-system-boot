package com.lw.graduation.infrastructure.mapper.selection;

import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;

/**
 * <p>
 * 选题记录表 Mapper 接口
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
public interface BizSelectionMapper extends MyBaseMapper<BizSelection> {
    // 继承MyBaseMapper的通用方法
    // selectDetailsWithRelations - 批量查询选题详情及关联信息
    // selectBatchWithOrder - 增强版批量查询
    // selectStatistics - 通用统计方法

    // 特定业务方法可在此添加
}
