package com.lw.graduation.common.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用枚举接口，所有业务枚举应实现此接口
 *
 * @param <T> Code 的类型 (如 Integer、String 等)
 * @author lw
 */
public interface IEnum<T> {

    /**
     * 获取枚举的 code(唯一标识)
     */
    T getCode();

    /**
     * 获取枚举的描述 (用于展示)
     */
    String getDescription();

    /**
     * 枚举代码缓存，避免重复遍历
     */
    Map<Class<?>, Map<?, ? extends IEnum<?>>> CACHE = new ConcurrentHashMap<>();

    /**
     * 根据 code 获取枚举实例 (使用 Map 缓存优化)
     *
     * @param enumClass 枚举类
     * @param code 枚举 code
     * @return 对应的枚举，未找到返回 null
     */
    @SuppressWarnings("unchecked")
    static <E extends IEnum<T>, T> E getByCode(Class<E> enumClass, T code) {
        if (code == null) {
            return null;
        }

        // 从缓存中获取该枚举类的映射表
        Map<?, ? extends IEnum<?>> enumMap = CACHE.computeIfAbsent(
            enumClass,
            cls -> {
                // 初始化缓存：遍历一次枚举值，构建 code -> enum 映射
                Map<Object, IEnum<?>> map = new ConcurrentHashMap<>();
                for (E enumConstant : enumClass.getEnumConstants()) {
                    map.put(enumConstant.getCode(), enumConstant);
                }
                return map;
            }
        );

        // 从缓存中快速获取
        return (E) enumMap.get(code);
    }

    /**
     * 判断 code 是否有效
     *
     * @param enumClass 枚举类
     * @param code 枚举 code
     * @return 有效返回 true
     */
    static <E extends IEnum<T>, T> boolean isValidCode(Class<E> enumClass, T code) {
        return getByCode(enumClass, code) != null;
    }
}
