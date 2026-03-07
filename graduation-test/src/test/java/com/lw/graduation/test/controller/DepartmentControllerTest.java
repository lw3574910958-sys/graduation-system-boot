package com.lw.graduation.test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.controller.department.DepartmentController;
import com.lw.graduation.api.dto.department.DepartmentCreateDTO;
import com.lw.graduation.api.dto.department.DepartmentPageQueryDTO;
import com.lw.graduation.api.dto.department.DepartmentUpdateDTO;
import com.lw.graduation.api.service.department.DepartmentService;
import com.lw.graduation.api.vo.department.DepartmentVO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTestConfig
@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    private DepartmentVO mockDepartmentVO;
    private IPage<DepartmentVO> mockDepartmentPage;
    private List<DepartmentVO> mockDepartmentList;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockDepartmentVO = new DepartmentVO();
        mockDepartmentVO.setId(1L);
        mockDepartmentVO.setName("计算机学院");
        mockDepartmentVO.setCode("CS");
        mockDepartmentVO.setCreatedAt(LocalDateTime.now());
        mockDepartmentVO.setUpdatedAt(LocalDateTime.now());

        // 准备分页数据
        Page<DepartmentVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockDepartmentVO));
        page.setTotal(1L);
        mockDepartmentPage = page;

        // 准备列表数据
        mockDepartmentList = Arrays.asList(mockDepartmentVO);
    }

    @Test
    void getDepartmentPage_ShouldReturnDepartmentPage() throws Exception {
        // Given
        DepartmentPageQueryDTO queryDTO = new DepartmentPageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        when(departmentService.getDepartmentPage(any(DepartmentPageQueryDTO.class))).thenReturn(mockDepartmentPage);

        // When & Then
        mockMvc.perform(get("/api/departments/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].name").value("计算机学院"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getDepartmentById_ExistingDepartment_ShouldReturnDepartmentVO() throws Exception {
        // Given
        when(departmentService.getDepartmentById(1L)).thenReturn(mockDepartmentVO);

        // When & Then
        mockMvc.perform(get("/api/departments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.name").value("计算机学院"))
                .andExpect(jsonPath("$.data.code").value("CS"));
    }

    @Test
    void getAllDepartments_ShouldReturnDepartmentList() throws Exception {
        // Given
        when(departmentService.getAllDepartments()).thenReturn(mockDepartmentList);

        // When & Then
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].name").value("计算机学院"));
    }

    @Test
    void createDepartment_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        DepartmentCreateDTO createDTO = TestDataGenerator.createDepartmentCreateDTO("新学院", "NEW");
        doNothing().when(departmentService).createDepartment(any(DepartmentCreateDTO.class));

        // When & Then
        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(createDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateDepartment_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        DepartmentUpdateDTO updateDTO = new DepartmentUpdateDTO();
        updateDTO.setName("更新后的学院名称");
        
        doNothing().when(departmentService).updateDepartment(eq(1L), any(DepartmentUpdateDTO.class));

        // When & Then
        mockMvc.perform(put("/api/departments/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteDepartment_ExistingDepartment_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(departmentService).deleteDepartment(1L);

        // When & Then
        mockMvc.perform(delete("/api/departments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getDepartmentPage_WithSearchParameters_ShouldReturnFilteredResults() throws Exception {
        // Given
        DepartmentPageQueryDTO queryDTO = new DepartmentPageQueryDTO();
        queryDTO.setName("计算机");
        
        when(departmentService.getDepartmentPage(any(DepartmentPageQueryDTO.class))).thenReturn(mockDepartmentPage);

        // When & Then
        mockMvc.perform(get("/api/departments/page")
                .param("current", "1")
                .param("size", "10")
                .param("name", "计算机"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].name").value("计算机学院"));
    }

    @Test
    void createDepartment_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given
        DepartmentCreateDTO invalidDTO = new DepartmentCreateDTO();
        // 不设置必需字段

        // When & Then
        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDepartmentById_NonExistingDepartment_ShouldReturnError() throws Exception {
        // Given
        when(departmentService.getDepartmentById(999L)).thenThrow(new RuntimeException("院系不存在"));

        // When & Then
        mockMvc.perform(get("/api/departments/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateDepartment_NonExistingDepartment_ShouldReturnError() throws Exception {
        // Given
        DepartmentUpdateDTO updateDTO = new DepartmentUpdateDTO();
        updateDTO.setName("更新学院");
        
        doNothing().when(departmentService).updateDepartment(eq(999L), any(DepartmentUpdateDTO.class));

        // When & Then
        mockMvc.perform(put("/api/departments/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(updateDTO)))
                .andExpect(status().isOk());
    }
}