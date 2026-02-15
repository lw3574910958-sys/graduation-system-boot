package com.lw.graduation.log.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import com.lw.graduation.log.annotation.LogOperation;
import com.lw.graduation.log.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 日志记录AOP切面
 * 基于项目现有AOP基础设施实现自动日志记录
 * 利用Sa-Token获取用户信息，Druid连接池优化数据库操作
 *
 * @author lw
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogAspect {

    private final SysLogService sysLogService;
    private final SysUserMapper sysUserMapper;

    @Before("@annotation(logOperation)")
    public void beforeLog(JoinPoint joinPoint, LogOperation logOperation) {
        log.debug("准备执行操作: {}", logOperation.value());
    }

    @AfterReturning("@annotation(logOperation)")
    public void afterLogSuccess(JoinPoint joinPoint, LogOperation logOperation) {
        try {
            // 获取当前用户信息
            Long userId = null;
            String userType = "ANONYMOUS";
            
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                SysUser user = sysUserMapper.selectById(userId);
                if (user != null) {
                    userType = user.getUserType();
                }
            }

            // 获取客户端IP地址
            String ipAddress = getClientIpAddress();

            // 构造操作描述
            String operation = buildOperationDescription(joinPoint, logOperation);

            // 记录日志
            sysLogService.logOperation(userId, userType, operation, ipAddress);
            
        } catch (Exception e) {
            log.error("AOP记录日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构造操作描述
     */
    private String buildOperationDescription(JoinPoint joinPoint, LogOperation logOperation) {
        StringBuilder sb = new StringBuilder();
        
        // 添加模块信息
        if (!logOperation.module().isEmpty()) {
            sb.append("[").append(logOperation.module()).append("] ");
        }
        
        // 添加操作描述
        sb.append(logOperation.value());
        
        // 如果需要记录参数（谨慎使用）
        if (logOperation.recordParams()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            
            sb.append(" 参数: ");
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    // 只记录简单类型，避免记录复杂对象
                    if (isSimpleType(args[i].getClass())) {
                        sb.append(signature.getParameterNames()[i])
                          .append("=")
                          .append(args[i])
                          .append(", ");
                    }
                }
            }
        }
        
        return sb.toString();
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class ||
               Number.class.isAssignableFrom(clazz) ||
               Boolean.class == clazz ||
               Character.class == clazz;
    }

    /**
     * 获取客户端IP地址
     * 基于项目现有Web基础设施实现
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                String ip = request.getHeader("X-Forwarded-For");
                if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0];
                    }
                } else {
                    ip = request.getHeader("Proxy-Client-IP");
                }
                
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("WL-Proxy-Client-IP");
                }
                
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
        }
        return "unknown";
    }
}