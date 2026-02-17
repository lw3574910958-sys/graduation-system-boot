package com.lw.graduation.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.domain.entity.log.SysLog;
import com.lw.graduation.infrastructure.mapper.log.SysLogMapper;
import com.lw.graduation.log.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统日志服务实现类
 * 基于项目现有基础设施（MyBatis-Plus、Druid连接池）的高性能实现
 * 支持增强版日志表的完整功能
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    private final SysLogMapper sysLogMapper;

    @Override
    @Async
    public void logOperation(Long userId, String userType, String operation, String ipAddress) {
        try {
            SysLog logEntry = new SysLog();
            logEntry.setUserId(userId);
            logEntry.setUserType(userType);
            logEntry.setOperation(operation);
            logEntry.setIpAddress(ipAddress);
            
            sysLogMapper.insert(logEntry);
            
            log.info("记录操作日志: 用户ID={}, 类型={}, 操作={}, IP={}", 
                    userId, userType, operation, ipAddress);
                    
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logOperationEnhanced(Long userId, String username, String userType, String module,
                                   String operation, Long businessId, Integer status, String ipAddress,
                                   Integer durationMs, String errorMessage) {
        try {
            SysLog logEntry = new SysLog();
            logEntry.setUserId(userId);
            logEntry.setUsername(username);
            logEntry.setUserType(userType);
            logEntry.setModule(module);
            logEntry.setOperation(operation);
            logEntry.setBusinessId(businessId);
            logEntry.setStatus(status);
            logEntry.setIpAddress(ipAddress);
            logEntry.setDurationMs(durationMs);
            logEntry.setErrorMessage(errorMessage);
            
            sysLogMapper.insert(logEntry);
            
            log.info("记录增强操作日志: 用户ID={}, 用户名={}, 模块={}, 操作={}, 状态={}, 耗时={}ms", 
                    userId, username, module, operation, status, durationMs);
                    
        } catch (Exception e) {
            log.error("记录增强操作日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logSecurityEvent(String username, String operation, String ipAddress, String remark) {
        try {
            // 使用预置的匿名用户ID记录安全事件
            Long userId = -1L;
            
            SysLog logEntry = new SysLog();
            logEntry.setUserId(userId);
            logEntry.setUsername(username);
            logEntry.setUserType("anonymous");
            logEntry.setModule("security");
            logEntry.setOperation(String.format("%s [%s]", operation, username));
            logEntry.setStatus(0); // 安全事件默认为失败状态
            logEntry.setIpAddress(ipAddress);
            logEntry.setErrorMessage(remark);
            
            sysLogMapper.insert(logEntry);
            
            log.warn("记录安全事件: 用户={}, 操作={}, IP={}, 备注={}", 
                    username, operation, ipAddress, remark);
                    
        } catch (Exception e) {
            log.error("记录安全事件日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void logBatch(Iterable<SysLog> logs) {
        try {
            List<SysLog> logList = new ArrayList<>();
            logs.forEach(logList::add);
            
            if (!logList.isEmpty()) {
                // 使用MyBatis-Plus批量插入优化
                saveBatch(logList);
                log.info("批量记录日志: {}条", logList.size());
            }
            
        } catch (Exception e) {
            log.error("批量记录日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredLogs(int retentionDays) {
        try {
            int deletedCount = sysLogMapper.deleteExpiredLogs(retentionDays);
            log.info("清理过期日志: 保留{}天，删除{}条记录", retentionDays, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期日志失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int getLogCountByModule(String module, int days) {
        try {
            return sysLogMapper.countByModule(module, days);
        } catch (Exception e) {
            log.error("按模块统计日志失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public int getFailedOperationCount(int days) {
        try {
            return sysLogMapper.countFailedOperations(days);
        } catch (Exception e) {
            log.error("统计失败操作失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public Double getAverageDuration(String module, int days) {
        try {
            return sysLogMapper.getAverageDuration(module, days);
        } catch (Exception e) {
            log.error("获取平均耗时失败: {}", e.getMessage(), e);
            return 0.0;
        }
    }
}