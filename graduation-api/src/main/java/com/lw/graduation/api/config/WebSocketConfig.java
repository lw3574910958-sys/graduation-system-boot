package com.lw.graduation.api.config;

import com.lw.graduation.api.websocket.AuthChannelInterceptor;
import com.lw.graduation.api.websocket.AuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 消息代理配置
 * 使用 STOMP 协议实现实时消息推送
 *
 * @author lw
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的内存消息代理
        // /topic - 广播模式（所有订阅者都会收到）
        // /queue - 点对点模式（只有一个消费者能收到）
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用端点前缀
        // 客户端发送消息到 /app 开头的目的地会被路由到 @MessageMapping 方法
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置用户相关消息的前缀
        registry.setUserDestinationPrefix("/user");
        
        log.info("WebSocket 消息代理配置完成");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点，客户端将连接此端点进行 WebSocket 通信
        registry.addEndpoint("/api/ws")
                .addInterceptors(new AuthHandshakeInterceptor()) // 添加握手拦截器进行 Token 认证
                .setAllowedOriginPatterns("*"); // 允许所有跨域请求
        
        log.info("WebSocket STOMP 端点已注册：/api/ws（原生 WebSocket）");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 在客户端入站通道添加认证拦截器
        registration.interceptors(authChannelInterceptor);
        log.info("WebSocket 认证拦截器已配置");
    }
}
