package com.lw.graduation.test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.controller.grade.GradeController;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeStatisticsQueryDTO;
import com.lw.graduation.api.service.grade.GradeService;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.test.config.WebMvcTestConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTestConfig
@WebMvcTest(GradeController.class)
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradeService gradeService;

    private GradeVO mockGradeVO;
    private IPage<GradeVO> mockGradePage;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockGradeVO = new GradeVO();
        mockGradeVO.setId(1L);
        mockGradeVO.setStudentId(1L);
        mockGradeVO.setStudentName("张三");
        mockGradeVO.setTopicId(1L);
        mockGradeVO.setTopicTitle("毕业设计课题");
        mockGradeVO.setScore(new java.math.BigDecimal("85.5"));
        mockGradeVO.setGraderId(2L);
        mockGradeVO.setGraderName("李老师");
        mockGradeVO.setComment("表现良好");
        mockGradeVO.setGradeLevel("良好");
        mockGradeVO.setGpa(new java.math.BigDecimal("3.5"));
        mockGradeVO.setPassing(true);
        mockGradeVO.setExcellent(false);
        mockGradeVO.setStudentNumber("2021001");
        mockGradeVO.setGradedAt(LocalDateTime.now());
        mockGradeVO.setCreatedAt(LocalDateTime.now());
        mockGradeVO.setUpdatedAt(LocalDateTime.now());

        // 准备分页数据
        Page<GradeVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockGradeVO));
        page.setTotal(1L);
        mockGradePage = page;
    }

    @Test
    void getGradePage_ShouldReturnGradePage() throws Exception {
        // Given
        GradePageQueryDTO queryDTO = new GradePageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        when(gradeService.getGradePage(any(GradePageQueryDTO.class))).thenReturn(mockGradePage);

        // When & Then
        mockMvc.perform(get("/api/grades/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].studentName").value("张三"))
                .andExpect(jsonPath("$.data.records[0].score").value(85.5))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getGradeById_ExistingGrade_ShouldReturnGradeVO() throws Exception {
        // Given
        when(gradeService.getGradeById(1L)).thenReturn(mockGradeVO);

        // When & Then
        mockMvc.perform(get("/api/grades/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.score").value(85.5))
                .andExpect(jsonPath("$.data.gradeLevel").value("良好"));
    }

    @Test
    void inputGrade_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        GradeInputDTO inputDTO = new GradeInputDTO();
        inputDTO.setStudentId(1L);
        inputDTO.setTopicId(1L);
        inputDTO.setScore(new java.math.BigDecimal("90.0"));
        inputDTO.setComment("优秀表现");
        when(gradeService.inputGrade(any(GradeInputDTO.class), any(Long.class))).thenReturn(mockGradeVO);

        // When & Then
        mockMvc.perform(post("/api/grades/input")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateGrade_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        GradeInputDTO inputDTO = new GradeInputDTO();
        inputDTO.setScore(new java.math.BigDecimal("92.5"));
        inputDTO.setComment("更新后的评语");
        
        when(gradeService.inputGrade(any(GradeInputDTO.class), any(Long.class))).thenReturn(mockGradeVO);

        // When & Then
        mockMvc.perform(put("/api/grades/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteGrade_ExistingGrade_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(gradeService).deleteGrade(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/grades/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getGradePage_WithSearchParameters_ShouldReturnFilteredResults() throws Exception {
        // Given
        GradePageQueryDTO queryDTO = new GradePageQueryDTO();
        queryDTO.setStudentId(1L);
        queryDTO.setTopicId(1L);
        queryDTO.setGraderId(2L);
        queryDTO.setMinScore(new java.math.BigDecimal("80.00"));
        queryDTO.setMaxScore(new java.math.BigDecimal("95.00"));
        
        when(gradeService.getGradePage(any(GradePageQueryDTO.class))).thenReturn(mockGradePage);

        // When & Then
        mockMvc.perform(get("/api/grades/page")
                .param("current", "1")
                .param("size", "10")
                .param("studentId", "1")
                .param("topicId", "1")
                .param("graderId", "2")
                .param("minScore", "80.00")
                .param("maxScore", "95.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].studentName").value("张三"));
    }

    @Test
    void getStudentGrades_ByStudentId_ShouldReturnStudentGrades() throws Exception {
        // Given
        when(gradeService.getGradesByStudent(1L)).thenReturn(Arrays.asList(mockGradeVO));

        // When & Then
        mockMvc.perform(get("/api/grades/student/{studentId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].studentName").value("张三"))
                .andExpect(jsonPath("$.data[0].score").value(85.5));
    }

    @Test
    void inputGrade_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given
        GradeInputDTO invalidDTO = new GradeInputDTO();
        // 不设置必需字段

        // When & Then
        mockMvc.perform(post("/api/grades/input")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGradeById_NonExistingGrade_ShouldReturnError() throws Exception {
        // Given
        when(gradeService.getGradeById(999L)).thenThrow(new RuntimeException("成绩记录不存在"));

        // When & Then
        mockMvc.perform(get("/api/grades/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getGradeStatistics_ByClass_ShouldReturnStatistics() throws Exception {
        // Given
        GradePageQueryDTO queryDTO = new GradePageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        queryDTO.setStudentId(1L);
        
        when(gradeService.getGradePage(any(GradePageQueryDTO.class))).thenReturn(mockGradePage);

        // When & Then
        mockMvc.perform(get("/api/grades/page")
                .param("current", "1")
                .param("size", "10")
                .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].studentName").value("张三"));
    }

    @Test
    void updateGrade_PartialUpdate_ShouldReturnSuccess() throws Exception {
        // Given
        GradeInputDTO inputDTO = new GradeInputDTO();
        inputDTO.setComment("部分更新评语");
        
        when(gradeService.inputGrade(any(GradeInputDTO.class), any(Long.class))).thenReturn(mockGradeVO);

        // When & Then
        mockMvc.perform(put("/api/grades/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(inputDTO)))
                .andExpect(status().isOk());
    }
}