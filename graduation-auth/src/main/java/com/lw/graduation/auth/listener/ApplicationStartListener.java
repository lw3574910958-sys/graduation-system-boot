package com.lw.graduation.auth.listener;

import com.lw.graduation.auth.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 * 用于处理用户类型变更后的缓存清理
 *
 * @author lw
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartListener {

    private final AuthServiceImpl authService;

    /**
     * 应用启动完成后执行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用启动完成，开始清除旧的用户缓存...");
        authService.clearAllUserCache();
        log.info("用户类型缓存清理完成，新的权限模型已生效");
    }
}
