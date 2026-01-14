package com.lw.graduation.common.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID 工具类
 * 后续可扩展雪花 ID、短 ID 等。
 *
 * @author lw
 */
public class IdUtil {

    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    /**
     * 生成 UUID（不含横线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
