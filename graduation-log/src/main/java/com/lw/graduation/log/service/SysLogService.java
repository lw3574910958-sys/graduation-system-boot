package com.lw.graduation.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lw.graduation.domain.entity.log.SysLog;

/**
 * 系统日志服务接口
 * 提供日志记录、查询、清理等核心功能
 * 支持增强版日志表的完整功能
 *
 * @author lw
 */
public interface SysLogService {

    /**
     * 记录操作日志（基础版本）
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @param operation 操作描述
     * @param ipAddress IP地址
     */
    void logOperation(Long userId, String userType, String operation, String ipAddress);

    /**
     * 记录操作日志（增强版本）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @param module 模块名称
     * @param operation 操作描述
     * @param businessId 业务ID
     * @param status 操作状态
     * @param ipAddress IP地址
     * @param durationMs 耗时(毫秒)
     * @param errorMessage 错误信息
     */
    void logOperationEnhanced(Long userId, String username, String userType, String module,
                            String operation, Long businessId, Integer status, String ipAddress,
                            Integer durationMs, String errorMessage);

    /**
     * 记录安全相关日志（如登录失败、账户锁定等）
     *
     * @param username 用户名
     * @param operation 操作描述
     * @param ipAddress IP地址
     * @param remark 备注信息
     */
    void logSecurityEvent(String username, String operation, String ipAddress, String remark);

    /**
     * 批量记录日志
     *
     * @param logs 日志列表
     */
    void logBatch(Iterable<SysLog> logs);

    /**
     * 清理过期日志
     *
     * @param retentionDays 保留天数
     * @return 删除记录数
     */
    int cleanupExpiredLogs(int retentionDays);

    /**
     * 按模块统计日志数量
     *
     * @param module 模块名称
     * @param days 统计天数
     * @return 日志数量
     */
    int getLogCountByModule(String module, int days);

    /**
     * 统计失败操作数量
     *
     * @param days 统计天数
     * @return 失败操作数量
     */
    int getFailedOperationCount(int days);

    /**
     * 获取模块平均操作耗时
     *
     * @param module 模块名称
     * @param days 统计天数
     * @return 平均耗时(毫秒)
     */
    Double getAverageDuration(String module, int days);
}