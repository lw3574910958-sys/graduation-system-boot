package com.lw.graduation.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

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
}