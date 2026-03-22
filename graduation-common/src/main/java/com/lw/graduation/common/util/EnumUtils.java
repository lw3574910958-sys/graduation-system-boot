package com.lw.graduation.common.util;


import com.lw.graduation.common.enums.IEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 枚举工具类：支持根据 code 快速、安全地查找枚举实例
 */
public final class EnumUtils {


    // 缓存：每个枚举类 -> (code -> enum实例)，code 类型由枚举自己决定
    private static final Map<Class<? extends IEnum<?>>, Map<?, ? extends IEnum<?>>> CACHE = new ConcurrentHashMap<>();

    // 私有构造函数，防止实例化
    private EnumUtils() {}

    /**
     * 根据 code 获取枚举（支持 Integer、String 等任意类型）
     */
    @SuppressWarnings("unchecked")
    public static <E extends IEnum<T>, T> E fromCode(Class<E> enumClass, T code) {
        if (code == null) {
            return null;
        }
        // 获取该枚举类的 code->enum 映射
        Map<T, E> codeMap = (Map<T, E>) CACHE
                .computeIfAbsent(enumClass, cls ->
                        Arrays.stream(cls.getEnumConstants())
                                .collect(Collectors.toMap(IEnum::getCode, Function.identity()))
                );
        return codeMap.get(code);
    }


    /**
     * 判断给定 code 是否有效（存在于该枚举中）
     */
    public static <E extends IEnum<T>, T> boolean isValidCode(Class<E> enumClass, T code) {
        return fromCode(enumClass, code) != null;
    }

}
