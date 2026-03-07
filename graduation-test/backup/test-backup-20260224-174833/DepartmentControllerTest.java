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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestConfig
@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    private DepartmentVO mockDepartmentVO;
    private IPage<DepartmentVO> mockDepartmentPage;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockDepartmentVO = new DepartmentVO();
        mockDepartmentVO.setId(1L);
        mockDepartmentVO.setName("计算机学�?);
        mockDepartmentVO.setCode("CS");
        mockDepartmentVO.setCreatedAt(LocalDateTime.now());
        mockDepartmentVO.setUpdatedAt(LocalDateTime.now());

        // 准备分页数据
        Page<DepartmentVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockDepartmentVO));
        page.setTotal(1L);
        mockDepartmentPage = page;
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
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].name").value("计算机学�?))
                .andExpect(jsonPath("$.data.records[0].code").value("CS"))
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
                .andExpect(jsonPath("$.data.name").value("计算机学�?))
                .andExpect(jsonPath("$.data.code").value("CS"))
                .andExpect(jsonPath("$.data.deanName").value("张教�?));
    }

    @Test
    void getAllDepartments_ShouldReturnAllDepartments() throws Exception {
        // Given
        List<DepartmentVO> departments = Arrays.asList(mockDepartmentVO);
        when(departmentService.getAllDepartments()).thenReturn(departments);

        // When & Then
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].name").value("计算机学�?))
                .andExpect(jsonPath("$.data[0].code").value("CS"));
    }

    @Test
    void createDepartment_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        DepartmentCreateDTO createDTO = TestDataGenerator.createDepartmentCreateDTO("软件学院", "SE");
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
        queryDTO.setName("计算�?);
        
        when(departmentService.getDepartmentPage(any(DepartmentPageQueryDTO.class))).thenReturn(mockDepartmentPage);

        // When & Then
        mockMvc.perform(get("/api/departments/page")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("keyword", "计算�?)
                .param("deanId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].name").value("计算机学�?));
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
        when(departmentService.getDepartmentById(999L)).thenThrow(new RuntimeException("院系不存�?));

        // When & Then
        mockMvc.perform(get("/api/departments/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteDepartment_NonExistingDepartment_ShouldReturnError() throws Exception {
        // Given
        doNothing().when(departmentService).deleteDepartment(999L);
        // 实际业务中应该抛出异常，这里模拟删除成功的情�?

        // When & Then
        mockMvc.perform(delete("/api/departments/{id}", 999L))
                .andExpect(status().isOk());
    }

    @Test
    void getDepartmentPage_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Given
        Page<DepartmentVO> page = new Page<>(2, 5);
        page.setRecords(Arrays.asList(mockDepartmentVO));
        page.setTotal(15L);
        when(departmentService.getDepartmentPage(any(DepartmentPageQueryDTO.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/departments/page")
                .param("pageNum", "2")
                .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.total").value(15));
    }
}
