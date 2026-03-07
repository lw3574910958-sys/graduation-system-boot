package com.lw.graduation.test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.controller.user.UserController;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.service.user.UserService;
import com.lw.graduation.api.vo.user.UserListInfoVO;
import com.lw.graduation.domain.enums.user.AccountStatus;
import com.lw.graduation.domain.enums.permission.SystemRole;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestConfig
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserListInfoVO mockUserVO;
    private IPage<UserListInfoVO> mockUserPage;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockUserVO = new UserListInfoVO();
        mockUserVO.setId(1L);
        mockUserVO.setUsername("testuser");
        mockUserVO.setRealName("测试用户");
        mockUserVO.setUserType("student");
        mockUserVO.setStatus(1);
        mockUserVO.setCreatedAt(LocalDateTime.now());
        mockUserVO.setUpdatedAt(LocalDateTime.now());
        mockUserVO.setLastLoginAt(LocalDateTime.now());
        mockUserVO.setAvatar("test-avatar.jpg");

        // 准备分页数据
        Page<UserListInfoVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockUserVO));
        page.setTotal(1L);
        mockUserPage = page;
    }

    @Test
    void getUserPage_ShouldReturnUserPage() throws Exception {
        // Given
        UserPageQueryDTO queryDTO = new UserPageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        when(userService.getUserPage(any(UserPageQueryDTO.class))).thenReturn(mockUserPage);

        // When & Then
        mockMvc.perform(get("/api/users/page")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUserVO() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(mockUserVO);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void createUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        UserCreateDTO createDTO = TestDataGenerator.createUserCreateDTO("newuser", "new@example.com");
        doNothing().when(userService).createUser(any(UserCreateDTO.class));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(createDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setRealName("更新用户");
        updateDTO.setUserType("teacher");
        updateDTO.setStatus(1);
        updateDTO.setAvatar("updated-avatar.jpg");
        
        doNothing().when(userService).updateUser(eq(1L), any(UserUpdateDTO.class));

        // When & Then
        mockMvc.perform(put("/api/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteUser_ExistingUser_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void resetPassword_ExistingUser_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(userService).resetPassword(1L);

        // When & Then
        mockMvc.perform(post("/api/users/{id}/reset-password", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getUserPage_WithSearchParameters_ShouldReturnFilteredResults() throws Exception {
        // Given
        UserPageQueryDTO queryDTO = new UserPageQueryDTO();
        queryDTO.setUsername("测试");
        
        when(userService.getUserPage(any(UserPageQueryDTO.class))).thenReturn(mockUserPage);

        // When & Then
        mockMvc.perform(get("/api/users/page")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("keyword", "测试")
                .param("role", "STUDENT")
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given
        UserCreateDTO invalidDTO = new UserCreateDTO();
        // 不设置必需字段

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_NonExistingUser_ShouldReturnError() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("用户不存�?));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }
}
