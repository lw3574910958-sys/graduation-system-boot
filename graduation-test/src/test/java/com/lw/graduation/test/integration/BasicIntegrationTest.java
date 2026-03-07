package com.lw.graduation.test.integration;

import com.lw.graduation.test.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 基础集成测试示例
 * 验证测试环境配置是否正确
 */
@TestConfig
@AutoConfigureMockMvc
class BasicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }

    @Test
    void testActuatorHealthEndpoint() throws Exception {
        // 测试基本的HTTP请求是否能正常工作
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isNotFound()); // 因为没有配置actuator，所以应该是404
    }
}