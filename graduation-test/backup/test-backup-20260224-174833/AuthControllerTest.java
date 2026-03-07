package com.lw.graduation.test.controller;

import com.lw.graduation.api.controller.auth.AuthController;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.api.vo.auth.CaptchaVO;
import com.lw.graduation.api.vo.auth.LoginVO;
import com.lw.graduation.common.response.Result;
import com.lw.graduation.test.config.WebMvcTestConfig;
import com.lw.graduation.test.util.TestDataGenerator;
import com.lw.graduation.test.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTestConfig
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private CaptchaVO mockCaptchaVO;
    private LoginVO mockLoginVO;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockCaptchaVO = new CaptchaVO();
        mockCaptchaVO.setCaptchaId("test-uuid");
        mockCaptchaVO.setCaptchaImg("data:image/png;base64,test-image-data");

        mockLoginVO = new LoginVO("test-token");
    }

    @Test
    void getCaptcha_ShouldReturnCaptchaVO() throws Exception {
        // Given
        when(authService.generateCaptchaDto()).thenReturn(mockCaptchaVO);

        // When & Then
        mockMvc.perform(get("/api/auth/captcha/get"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.captchaId").value("test-uuid"))
                .andExpect(jsonPath("$.data.captchaImg").value("data:image/png;base64,test-image-data"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginVO() throws Exception {
        // Given
        LoginDTO loginDTO = TestDataGenerator.createLoginDTO("testuser", "123456");
        when(authService.login(any(LoginDTO.class))).thenReturn("test-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.token").value("test-token"));

    }

    @Test
    void logout_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(authService).logout();

        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));

    }

    @Test
    void getCurrentUserInfo_ShouldReturnUserInfo() throws Exception {
        // Given
        // 这里需要模拟Sa-Token的登录状�?
        // 在实际测试中可能需要使用@WithMockUser或其他方式模拟认�?

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void refreshToken_ShouldReturnNewToken() throws Exception {
        // Given
        when(authService.refreshToken()).thenReturn("new-test-token");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.token").value("new-test-token"));
    }

    @Test
    void checkCaptcha_WithValidCode_ShouldReturnTrue() throws Exception {
        // Given
        when(authService.checkCaptcha("test-uuid", "123456")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/captcha/check")
                .param("captchaKey", "test-uuid")
                .param("captchaCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.valid").value(true));
    }

    @Test
    void login_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given
        LoginDTO invalidLoginDTO = new LoginDTO();
        // 不设置必需字段

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(invalidLoginDTO)))
                .andExpect(status().isBadRequest());
    }
}
