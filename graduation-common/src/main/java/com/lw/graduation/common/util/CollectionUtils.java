package com.lw.graduation.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具类
 * 提供常用的集合操作方法，减少重复代码
 *
 * @author lw
 */
public class CollectionUtils {
    
    /**
     * 从实体列表中提取ID列表（处理空值检查）
     * 
     * @param entities 实体列表
     * @param idExtractor ID提取函数
     * @param <T> 实体类型
     * @return ID列表，如果输入为空则返回空列表
     */
    public static <T> List<Long> extractIds(List<T> entities, Function<T, Long> idExtractor) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        return entities.stream()
                .map(idExtractor)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查集合并提供默认值
     * 
     * @param collection 集合
     * @param defaultValue 默认值
     * @param <T> 集合元素类型
     * @return 如果集合为空则返回默认值，否则返回原集合
     */
    public static <T> List<T> defaultIfEmpty(List<T> collection, List<T> defaultValue) {
        if (collection == null || collection.isEmpty()) {
            return defaultValue != null ? defaultValue : new ArrayList<>();
        }
        return collection;
    }
    
    /**
     * 安全地检查集合是否为空
     * 
     * @param collection 集合
     * @return 如果集合为null或空则返回true
     */
    public static boolean isEmpty(List<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * 安全地检查集合是否非空
     * 
     * @param collection 集合
     * @return 如果集合非null且非空则返回true
     */
    public static boolean isNotEmpty(List<?> collection) {
        return !isEmpty(collection);
    }
}