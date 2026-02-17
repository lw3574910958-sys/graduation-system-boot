package com.lw.graduation.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bean 映射工具类
 * 提供通用的对象属性拷贝和转换功能，减少重复代码
 *
 * @author lw
 */
@Slf4j
public class BeanMapperUtil {

    /**
     * 单个对象属性拷贝
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 目标对象实例
     */
    public static <S, T> T copyProperties(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("对象属性拷贝失败: source={}, targetClass={}, error={}", 
                     source.getClass().getSimpleName(), targetClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("对象转换失败", e);
        }
    }

    /**
     * 单个对象属性拷贝（带自定义转换）
     *
     * @param source 源对象
     * @param targetClass 目标类
     * @param customConverter 自定义转换函数
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 目标对象实例
     */
    public static <S, T> T copyProperties(S source, Class<T> targetClass, 
                                        Function<T, T> customConverter) {
        T target = copyProperties(source, targetClass);
        if (target != null && customConverter != null) {
            target = customConverter.apply(target);
        }
        return target;
    }

    /**
     * 列表对象属性拷贝
     *
     * @param sourceList 源对象列表
     * @param targetClass 目标类
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyProperties(List<S> sourceList, Class<T> targetClass) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        
        return sourceList.stream()
                .map(source -> copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 列表对象属性拷贝（带自定义转换）
     *
     * @param sourceList 源对象列表
     * @param targetClass 目标类
     * @param customConverter 自定义转换函数
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyProperties(List<S> sourceList, Class<T> targetClass,
                                              Function<T, T> customConverter) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        
        return sourceList.stream()
                .map(source -> copyProperties(source, targetClass, customConverter))
                .collect(Collectors.toList());
    }

    /**
     * 带过滤条件的列表转换
     *
     * @param sourceList 源对象列表
     * @param targetClass 目标类
     * @param filter 过滤条件
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 过滤后的目标对象列表
     */
    public static <S, T> List<T> copyPropertiesWithFilter(List<S> sourceList, Class<T> targetClass,
                                                        Function<S, Boolean> filter) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        
        return sourceList.stream()
                .filter(source -> filter == null || filter.apply(source))
                .map(source -> copyProperties(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 复杂对象转换（需要手动设置特殊字段）
     *
     * @param source 源对象
     * @param converter 转换函数
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 转换后的对象
     */
    public static <S, T> T convert(S source, Function<S, T> converter) {
        if (source == null || converter == null) {
            return null;
        }
        return converter.apply(source);
    }

    /**
     * 复杂列表转换
     *
     * @param sourceList 源对象列表
     * @param converter 转换函数
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @return 转换后的列表
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Function<S, T> converter) {
        if (CollectionUtils.isEmpty(sourceList) || converter == null) {
            return Collections.emptyList();
        }
        
        return sourceList.stream()
                .map(converter)
                .collect(Collectors.toList());
    }
}