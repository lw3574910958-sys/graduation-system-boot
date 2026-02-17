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

    /**
     * 带缓存的数据获取方法（推荐使用）
     * 自动处理缓存穿透和数据加载
     *
     * @param key 缓存键
     * @param clazz 返回类型
     * @param loader 数据加载函数
     * @param expireSeconds 过期时间（秒）
     * @param <T> 泛型类型
     * @return 缓存或加载的数据
     */
    public <T> T getFromCache(String key, Class<T> clazz, java.util.function.Supplier<T> loader, int expireSeconds) {
        // 1. 先从缓存获取
        T cached = getFromCache(key, clazz);
        if (cached != null) {
            return cached;
        }
        
        // 2. 缓存未命中，加载数据
        T data = loader.get();
        if (data == null) {
            // 3. 数据为空，缓存空值标记
            putNullMarker(key);
        } else {
            // 4. 数据不为空，缓存数据
            putToCache(key, data, expireSeconds);
        }
        
        return data;
    }

    /**
     * 简化版缓存获取（适用于已有数据的情况）
     *
     * @param key 缓存键
     * @param data 数据
     * @param expireSeconds 过期时间
     * @param <T> 泛型类型
     * @return 数据
     */
    public <T> T cacheIfAbsent(String key, T data, int expireSeconds) {
        if (data == null) {
            putNullMarker(key);
            return null;
        }
        putToCache(key, data, expireSeconds);
        return data;
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存键失败: key={}, error={}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取缓存剩余过期时间
     *
     * @param key 缓存键
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("获取缓存过期时间失败: key={}, error={}", key, e.getMessage(), e);
            return -2;
        }
    }
}