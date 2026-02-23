package com.lw.graduation.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lw.graduation.domain.entity.log.SysLog;

/**
 * 系统日志服务接口
 * 继承MyBatis-Plus IService接口，获得丰富的内置CRUD功能
 * 提供日志记录、查询、清理等核心功能
 * 支持增强版日志表的完整功能
 *
 * @author lw
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 记录操作日志（基础版本）
     * 当前项目主要使用的日志记录方法
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @param operation 操作描述
     * @param ipAddress IP地址
     */
    void logOperation(Long userId, String userType, String operation, String ipAddress);
    
    // 以下方法为扩展功能，可根据实际需求逐步启用
    // logOperationEnhanced - 增强版日志记录
    // logSecurityEvent - 安全日志记录  
    // logBatch - 批量日志记录
    // cleanupExpiredLogs - 过期日志清理
    // getLogCountByModule - 按模块统计
    // getFailedOperationCount - 失败操作统计
    // getAverageDuration - 平均耗时统计
}