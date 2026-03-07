package com.lw.graduation.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 测试环境事务管理器配置
 * 解决@WebMvcTest环境中缺少PlatformTransactionManager的问题
 *
 * @author lw
 */
@TestConfiguration
public class TestTransactionConfig {

    /**
     * 配置嵌入式数据库用于测试
     * 为@WebMvcTest环境提供数据源支持
     */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    /**
     * 配置平台事务管理器
     * 为@WebMvcTest环境提供事务支持
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        // 在测试环境中使用简单的事务管理器
        return new org.springframework.jdbc.datasource.DataSourceTransactionManager(dataSource);
    }

    /**
     * 配置事务模板
     * 便于在测试中进行编程式事务管理
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}