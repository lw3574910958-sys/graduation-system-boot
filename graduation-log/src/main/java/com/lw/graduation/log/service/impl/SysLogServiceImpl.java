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
 * 继承MyBatis-Plus ServiceImpl，获得丰富的内置CRUD功能
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
    
    // 以下为扩展功能实现，可根据实际需求逐步启用
    // logOperationEnhanced - 增强版日志记录
    // logSecurityEvent - 安全日志记录  
    // logBatch - 批量日志记录
    // cleanupExpiredLogs - 过期日志清理
    // getLogCountByModule - 按模块统计
    // getFailedOperationCount - 失败操作统计
    // getAverageDuration - 平均耗时统计
}