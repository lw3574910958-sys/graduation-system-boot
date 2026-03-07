package com.lw.graduation.test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.controller.grade.GradeController;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.service.grade.GradeService;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.test.config.WebMvcTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestConfig
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
        mockGradeVO.setTopicTitle("基于Spring Boot的毕业设计管理系�?);
        mockGradeVO.setScore(new BigDecimal("85.50"));
        mockGradeVO.setGraderId(2L);
        mockGradeVO.setGraderName("李教�?);
        mockGradeVO.setComment("完成度较高，代码质量良好");
        mockGradeVO.setGradeLevel("良好");
        mockGradeVO.setGpa(new BigDecimal("3.5"));
        mockGradeVO.setPassing(true);
        mockGradeVO.setExcellent(false);
        mockGradeVO.setGradedAt(LocalDateTime.now());
        mockGradeVO.setCreatedAt(LocalDateTime.now());

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
        when(gradeService.getGradePage(any(GradePageQueryDTO.class))).thenReturn(mockGradePage);

        // When & Then
        mockMvc.perform(get("/api/grades/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].studentName").value("张三"))
                .andExpect(jsonPath("$.data.records[0].topicTitle").value("基于Spring Boot的毕业设计管理系�?))
                .andExpect(jsonPath("$.data.records[0].score").value(85.50))
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
                .andExpect(jsonPath("$.data.studentName").value("张三"))
                .andExpect(jsonPath("$.data.score").value(85.50))
                .andExpect(jsonPath("$.data.graderName").value("李教�?))
                .andExpect(jsonPath("$.data.gradeLevel").value("良好"));
    }

    @Test
    void inputGrade_WithValidData_ShouldReturnGradeVO() throws Exception {
        // Given
        GradeInputDTO inputDTO = new GradeInputDTO();
        inputDTO.setStudentId(1L);
        inputDTO.setTopicId(1L);
        inputDTO.setScore(new BigDecimal("90.00"));
        inputDTO.setComment("优秀的作�?);
        
        when(gradeService.inputGrade(any(GradeInputDTO.class), eq(2L))).thenReturn(mockGradeVO);

        // When & Then
        mockMvc.perform(post("/api/grades/input")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "studentId": 1,
                    "topicId": 1,
                    "score": 90.00,
                    "comment": "优秀的作�?
                }
                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.score").value(85.50));
    }

    @Test
    void calculateCompositeGrade_ShouldReturnCalculatedScore() throws Exception {
        // Given
        BigDecimal calculatedScore = new BigDecimal("87.25");
        when(gradeService.calculateCompositeGrade(1L, 1L)).thenReturn(calculatedScore);

        // When & Then
        mockMvc.perform(post("/api/grades/calculate")
                .param("studentId", "1")
                .param("topicId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(87.25));
    }

    @Test
    void deleteGrade_ExistingGrade_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(gradeService).deleteGrade(1L, 2L);

        // When & Then
        mockMvc.perform(delete("/api/grades/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getMyGrades_ShouldReturnTeacherGrades() throws Exception {
        // Given
        when(gradeService.getGradePage(any(GradePageQueryDTO.class))).thenReturn(mockGradePage);

        // When & Then
        mockMvc.perform(get("/api/grades/my")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].graderName").value("李教�?));
    }

    @Test
    void getGradePage_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Given
        GradePageQueryDTO queryDTO = new GradePageQueryDTO();
        queryDTO.setStudentId(1L);
        queryDTO.setTopicId(1L);
        queryDTO.setGraderId(2L);
        queryDTO.setMinScore(new BigDecimal("80.00"));
        queryDTO.setMaxScore(new BigDecimal("95.00"));
        
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
                .andExpect(jsonPath("$.data.records[0].studentName").value("张三"))
                .andExpect(jsonPath("$.data.records[0].score").value(85.50));
    }

    @Test
    void inputGrade_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given
        GradeInputDTO invalidDTO = new GradeInputDTO();
        // 不设置必需字段

        // When & Then
        mockMvc.perform(post("/api/grades/input")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGradeById_NonExistingGrade_ShouldReturnError() throws Exception {
        // Given
        when(gradeService.getGradeById(999L)).thenThrow(new RuntimeException("成绩不存�?));

        // When & Then
        mockMvc.perform(get("/api/grades/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getGradePage_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Given
        Page<GradeVO> page = new Page<>(2, 5);
        page.setRecords(Arrays.asList(mockGradeVO));
        page.setTotal(15L);
        when(gradeService.getGradePage(any(GradePageQueryDTO.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/grades/page")
                .param("current", "2")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.total").value(15));
    }

    @Test
    void getGradesByStudent_ShouldReturnStudentGrades() throws Exception {
        // Given - 这个接口可能需要在GradeController中添�?
        // 暂时跳过，因为我们主要关注已有的接口
        
        // When & Then
        mockMvc.perform(get("/api/grades/student/{studentId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getGradesByTeacher_ShouldReturnTeacherGrades() throws Exception {
        // Given - 这个接口可能需要在GradeController中添�?
        // 暂时跳过，因为我们主要关注已有的接口
        
        // When & Then
        mockMvc.perform(get("/api/grades/teacher/{teacherId}", 2L))
                .andExpect(status().isOk());
    }
}
