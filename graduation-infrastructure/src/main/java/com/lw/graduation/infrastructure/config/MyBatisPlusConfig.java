package com.lw.graduation.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
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
     * 添加分页插件、逻辑删除插件等
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        paginationInnerInterceptor.setOverflow(true);
        paginationInnerInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 2. 乐观锁插件（可选）
        // interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防止全表更新与删除插件（可选）
        // interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 注意：逻辑删除不需要插件，通过注解和配置自动生效
        // MyBatis-Plus 3.4.0+ 版本，@TableLogic 注解会自动处理逻辑删除
        // 无需添加 LogicDeleteInnerInterceptor

        return interceptor;
    }
}