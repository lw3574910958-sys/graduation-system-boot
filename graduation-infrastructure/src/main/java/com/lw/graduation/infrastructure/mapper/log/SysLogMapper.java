package com.lw.graduation.infrastructure.mapper.log;

import com.lw.graduation.domain.entity.log.SysLog;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 系统日志表 Mapper 接口
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
@Mapper
public interface SysLogMapper extends MyBaseMapper<SysLog> {
    
    /**
     * 清理过期日志
     * 根据保留天数删除历史日志
     * 
     * @param days 保留天数
     * @return 删除记录数
     */
    int deleteExpiredLogs(@Param("days") int days);
    
    /**
     * 按模块统计日志数量
     * 
     * @param module 模块名称
     * @param days 统计天数
     * @return 日志数量
     */
    int countByModule(@Param("module") String module, @Param("days") int days);
    
    /**
     * 统计失败操作数量
     * 
     * @param days 统计天数
     * @return 失败操作数量
     */
    int countFailedOperations(@Param("days") int days);
    
    /**
     * 获取平均操作耗时
     * 
     * @param module 模块名称
     * @param days 统计天数
     * @return 平均耗时(毫秒)
     */
    Double getAverageDuration(@Param("module") String module, @Param("days") int days);
}
