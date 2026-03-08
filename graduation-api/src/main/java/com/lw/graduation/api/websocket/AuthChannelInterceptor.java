package com.lw.graduation.api.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 认证拦截器
 * 用于验证 STOMP 连接时的 Token
 *
 * @author lw
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthChannelInterceptor implements ChannelInterceptor {

    private static final String TOKEN_HEADER = "token";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
    
        if (StompCommand.CONNECT == command) {
            // 获取客户端传递的 token（从 header 中获取）
            String token = accessor.getFirstNativeHeader(TOKEN_HEADER);
                
            log.debug("WebSocket 连接请求，token: {}", token != null ? "存在" : "缺失");
    
            try {
                if (token == null || token.trim().isEmpty()) {
                    log.warn("WebSocket 连接失败：缺少认证 token");
                    throw new IllegalArgumentException("缺少认证 token");
                }
            
                // 去掉 "Bearer " 前缀（如果存在）
                String cleanToken = token.replaceFirst("^Bearer\\s+", "").trim();
                            
                if (cleanToken.isEmpty()) {
                    throw new IllegalArgumentException("token 为空");
                }
            
                // 使用 Sa-Token 验证 token
                // 通过 token 获取登录 ID
                Object loginId = StpUtil.getLoginIdByToken(cleanToken);
                            
                if (loginId == null) {
                    log.warn("WebSocket 认证失败：token 无效或已过期");
                    throw new IllegalArgumentException("无效的 token");
                }
                            
                // 将用户 ID 设置到 header 中（用于当前消息）
                accessor.setUser(() -> loginId.toString());
                
                // 将用户 ID 存储到 session attributes 中（用于后续订阅）
                accessor.getSessionAttributes()
                        .put("userId", loginId.toString());
                
                log.info("WebSocket 认证成功 - 用户 ID: {}", loginId);
                            
            } catch (Exception e) {
                log.error("WebSocket 认证失败：{}", e.getMessage());
                throw new IllegalArgumentException("认证失败：" + e.getMessage(), e);
            }
        } else if (StompCommand.SUBSCRIBE == command) {
            // 订阅时也检查认证
            // 优先从 session attributes 获取用户 ID
            Object userId = accessor.getSessionAttributes().get("userId");
            
            if (userId == null) {
                // 如果 session attributes 中没有，尝试从 accessor 获取
                Object user = accessor.getUser();
                if (user == null) {
                    log.warn("WebSocket 订阅失败：用户未认证");
                    throw new IllegalStateException("用户未认证，无法订阅");
                }
                userId = user;
            }
            
            log.debug("WebSocket 订阅成功 - 用户 ID: {}, 订阅目标：{}", 
                     userId, accessor.getDestination());
        }
    
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.debug("WebSocket连接成功：{}", accessor.getSessionId());
        }
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            log.error("WebSocket 消息发送失败：{}", ex.getMessage());
        }
    }
}
