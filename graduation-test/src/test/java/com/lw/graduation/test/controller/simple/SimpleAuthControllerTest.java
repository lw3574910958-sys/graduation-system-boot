package com.lw.graduation.test.controller.simple;

import com.lw.graduation.api.controller.auth.AuthController;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.test.config.WebMvcTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 简化版认证控制器测试
 * 用于验证测试环境配置是否正确
 *
 * @author lw
 */
@WebMvcTestConfig
@WebMvcTest(AuthController.class)
class SimpleAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void healthCheck_ShouldReturnOk() throws Exception {
        // 简单的健康检查测试
        mockMvc.perform(get("/api/auth/captcha/get"))
                .andExpect(status().isOk());
    }
}