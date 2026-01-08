package com.lw.graduation.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus 配置
 *
 * @author lw
 */
@Configuration
@MapperScan("com.lw.graduation.infrastructure.mapper.**")
public class MyBatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 创建分页插件实例
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();

        // --- 常用配置 ---
        // 1. 指定数据库类型 (非常重要!)
        paginationInnerInterceptor.setDbType(DbType.MYSQL);

        // 2. 配置请求页码大于最大页时的操作， true调回到首页 (可选)
        paginationInnerInterceptor.setOverflow(true);

        // 3. 配置最大单页限制数量，防止内存溢出 (推荐)
        paginationInnerInterceptor.setMaxLimit(1000L); // 例如限制最大1000条/页

        // 将配置好的分页插件添加到拦截器链中
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 如果以后需要添加其他插件，记得分页插件一般最后添加
        // interceptor.addInnerInterceptor(new OtherInnerInterceptor());

        return interceptor;
    }
}