package com.lw.graduation.api.websocket;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 用于在握手阶段验证 Token
 *
 * @author lw
 */
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final String TOKEN_PARAM = "token";

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {
        boolean result = false;
        if (request instanceof ServletServerHttpRequest serverRequest) {
            String token = serverRequest.getServletRequest().getParameter(TOKEN_PARAM);

            log.debug("WebSocket 握手请求，token: {}", token != null ? "存在" : "缺失");

            try {
                if (token == null || token.trim().isEmpty()) {
                    log.warn("WebSocket 握手失败：缺少认证 token");
                } else {// 去掉 "Bearer " 前缀（如果存在）
                    String cleanToken = token.replaceFirst("^Bearer\\s+", "").trim();
                    if (cleanToken.isEmpty()) {
                        log.warn("WebSocket 握手失败：token 为空");
                    } else {// 使用 Sa-Token 验证 token
                        Object loginId = StpUtil.getLoginIdByToken(cleanToken);
                        if (loginId == null) {
                            log.warn("WebSocket 认证失败：token 无效或已过期");
                        } else {// 将用户 ID 存储到 attributes 中，供后续使用
                            attributes.put("userId", loginId.toString());
                            log.info("WebSocket 握手成功 - 用户 ID: {}", loginId);
                            result = true;
                        }
                    }
                }

            } catch (Exception e) {
                log.error("WebSocket 认证失败：{}", e.getMessage());
            }
        }

        return result;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, 
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, 
                               @Nullable Exception exception) {
        // 握手后处理，可以用于日志记录等
    }
}
