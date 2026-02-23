/*
 * Redis缓存工具类（已废弃）
 * 项目已统一使用CacheHelper进行缓存操作
 * 此类功能与CacheHelper重复，建议删除
 *
 * @author lw
 * @deprecated 使用 {@link CacheHelper} 替代
 *
package com.lw.graduation.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存工具类
 * 提供常用的缓存操作方法
 *
 * @author lw
 * @deprecated 使用 {@link CacheHelper} 替代
 */
//@Slf4j
//@Component
//public class RedisCacheUtil {

    /*
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    /*
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 设置缓存并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    /*
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    /*
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 获取缓存并转换为指定类型
     *
     * @param key   键
     * @param clazz 类型
     * @param <T>   泛型
     * @return 值
     */
    /*
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return (T) value;
            }
            return null;
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否删除成功
     */
    /*
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    /*
    public long delete(Collection<String> keys) {
        try {
            Long result = redisTemplate.delete(keys);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("批量删除缓存失败，keys: {}, error: {}", keys, e.getMessage());
            return 0;
        }
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    /*
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("判断缓存是否存在失败，key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    /*
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("设置过期时间失败，key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取剩余过期时间
     *
     * @param key  键
     * @param unit 时间单位
     * @return 剩余时间
     */
    /*
    public Long getExpire(String key, TimeUnit unit) {
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("获取过期时间失败，key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }
    */
//}

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 设置缓存并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 获取缓存并转换为指定类型
     *
     * @param key   键
     * @param clazz 类型
     * @param <T>   泛型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return (T) value;
            }
            return null;
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    public long delete(Collection<String> keys) {
        try {
            Long result = redisTemplate.delete(keys);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("批量删除缓存失败，keys: {}, error: {}", keys, e.getMessage());
            return 0;
        }
    }

    /**
     * 判断缓存是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("判断缓存是否存在失败，key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("设置过期时间失败，key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取剩余过期时间
     *
     * @param key  键
     * @param unit 时间单位
     * @return 剩余时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("获取过期时间失败，key: {}, error: {}", key, e.getMessage());
            return null;
        }
    }
}