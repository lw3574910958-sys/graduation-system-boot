package com.lw.graduation.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 自定义基础 Mapper
 *
 * @param <T>
 */
public interface MyBaseMapper<T> extends BaseMapper<T> {
    // 可在此添加通用方法，如 batchInsert 等
}