package com.lw.graduation.common.util;

import com.lw.graduation.common.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 通用缓存操作工具类
 * 提供统一的缓存读写操作，避免重复代码
 *
 * @author lw
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 从缓存中获取数据
     *
     * @param key 缓存键
     * @param clazz 返回类型
     * @param <T> 泛型类型
     * @return 缓存数据，如果不存在或为空值标记则返回null
     */
    public <T> T getFromCache(String key, Class<T> clazz) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                if (CacheConstants.CacheValue.NULL_MARKER.equals(cached)) {
                    log.debug("缓存命中空值标记: {}", key);
                    return null;
                }
                return clazz.cast(cached);
            }
            return null;
        } catch (Exception e) {
            log.error("从缓存获取数据失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将数据放入缓存
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    public void putToCache(String key, Object value, int expireSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
            log.debug("数据已缓存: key={}, expire={}s", key, expireSeconds);
        } catch (Exception e) {
            log.error("缓存数据失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 缓存空值标记（防止缓存穿透）
     *
     * @param key 缓存键
     */
    public void putNullMarker(String key) {
        try {
            redisTemplate.opsForValue().set(
                key,
                CacheConstants.CacheValue.NULL_MARKER,
                CacheConstants.CacheValue.NULL_EXPIRE,
                TimeUnit.SECONDS
            );
            log.debug("空值标记已缓存: {}", key);
        } catch (Exception e) {
            log.error("缓存空值标记失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 清除缓存
     *
     * @param key 缓存键
     */
    public void evictCache(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("缓存已清除: {}", key);
        } catch (Exception e) {
            log.error("清除缓存失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 批量清除缓存
     *
     * @param keys 缓存键数组
     */
    public void evictCaches(String... keys) {
        try {
            for (String key : keys) {
                redisTemplate.delete(key);
                log.debug("缓存已清除: {}", key);
            }
        } catch (Exception e) {
            log.error("批量清除缓存失败: error={}", e.getMessage(), e);
        }
    }
}