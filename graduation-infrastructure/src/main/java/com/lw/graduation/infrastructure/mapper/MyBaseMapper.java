package com.lw.graduation.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 自定义基础 Mapper
 * 提供通用的批量查询和性能优化方法
 *
 * @param <T> 实体类型
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {
    
    /**
     * 通用批量查询详情及关联信息（优化N+1查询问题）
     * 各子Mapper需要在XML中提供具体的实现
     * 
     * @param ids 主键ID列表
     * @return 包含详情和关联信息的结果列表
     */
    List<Map<String, Object>> selectDetailsWithRelations(@Param("ids") List<Long> ids);
    
    /**
     * 通用批量查询（MyBatis-Plus selectBatchIds的增强版）
     * 支持自定义排序和字段选择
     * 
     * @param ids 主键ID列表
     * @return 实体列表
     */
    List<T> selectBatchWithOrder(@Param("ids") List<Long> ids);
    
    /**
     * 通用统计方法
     * 
     * @param condition 查询条件
     * @return 统计结果
     */
    Map<String, Object> selectStatistics(@Param("condition") Map<String, Object> condition);
}