package com.lw.graduation.infrastructure.mapper.topic;

import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;

/**
 * <p>
 * 题目表 Mapper 接口
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
public interface BizTopicMapper extends MyBaseMapper<BizTopic> {
    // 继承MyBaseMapper的通用方法
    // selectDetailsWithRelations - 批量查询题目详情及关联信息
    // selectBatchWithOrder - 增强版批量查询
    // selectStatistics - 通用统计方法

    // 特定业务方法可在此添加
}
