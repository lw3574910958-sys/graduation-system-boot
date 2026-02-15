package com.lw.graduation.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lw.graduation.log.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志Mapper接口
 * 基于MyBatis-Plus提供高效的CRUD操作
 * 支持增强版日志表的所有字段
 *
 * @author lw
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
    
    /**
     * 批量插入日志记录
     * 利用MyBatis-Plus的批量操作优化性能
     * 
     * @param logs 日志列表
     * @return 插入记录数
     */
    int insertBatchSomeColumn(java.util.List<SysLog> logs);
    
    /**
     * 清理过期日志
     * 根据保留天数删除历史日志
     * 
     * @param days 保留天数
     * @return 删除记录数
     */
    int deleteExpiredLogs(int days);
    
    /**
     * 按模块统计日志数量
     * 
     * @param module 模块名称
     * @param days 统计天数
     * @return 日志数量
     */
    int countByModule(String module, int days);
    
    /**
     * 统计失败操作数量
     * 
     * @param days 统计天数
     * @return 失败操作数量
     */
    int countFailedOperations(int days);
    
    /**
     * 获取平均操作耗时
     * 
     * @param module 模块名称
     * @param days 统计天数
     * @return 平均耗时(毫秒)
     */
    Double getAverageDuration(String module, int days);
}