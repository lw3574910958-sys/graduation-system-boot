package com.lw.graduation.test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.controller.topic.TopicController;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.topic.TopicPageQueryDTO;
import com.lw.graduation.api.dto.topic.TopicUpdateDTO;
import com.lw.graduation.api.service.topic.TopicService;
import com.lw.graduation.api.vo.topic.TopicVO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTestConfig
@WebMvcTest(TopicController.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    private TopicVO mockTopicVO;
    private IPage<TopicVO> mockTopicPage;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockTopicVO = new TopicVO();
        mockTopicVO.setId(1L);
        mockTopicVO.setTitle("基于Spring Boot的毕业设计管理系统");
        mockTopicVO.setDescription("设计并实现一个完整的毕业设计管理系统");
        mockTopicVO.setSource("企业合作");
        mockTopicVO.setType("应用研究");
        mockTopicVO.setNature("理论研究");
        mockTopicVO.setDifficulty(3);
        mockTopicVO.setWorkload(3);
        mockTopicVO.setMaxSelections(3);
        mockTopicVO.setSelectedCount(1);
        mockTopicVO.setStatus(1);
        mockTopicVO.setCreatedAt(LocalDateTime.now());
        mockTopicVO.setUpdatedAt(LocalDateTime.now());

        // 准备分页数据
        Page<TopicVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockTopicVO));
        page.setTotal(1L);
        mockTopicPage = page;
    }

    @Test
    void getTopicPage_ShouldReturnTopicPage() throws Exception {
        // Given
        TopicPageQueryDTO queryDTO = new TopicPageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        when(topicService.getTopicPage(any(TopicPageQueryDTO.class))).thenReturn(mockTopicPage);

        // When & Then
        mockMvc.perform(get("/api/topics/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].title").value("基于Spring Boot的毕业设计管理系统"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getTopicById_ExistingTopic_ShouldReturnTopicVO() throws Exception {
        // Given
        when(topicService.getTopicById(1L)).thenReturn(mockTopicVO);

        // When & Then
        mockMvc.perform(get("/api/topics/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.title").value("基于Spring Boot的毕业设计管理系统"))
                .andExpect(jsonPath("$.data.description").value("设计并实现一个完整的毕业设计管理系统"));
    }

    @Test
    void createTopic_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        TopicCreateDTO createDTO = TestDataGenerator.createTopicCreateDTO("新的课题标题", 1L);
        doNothing().when(topicService).createTopic(any(TopicCreateDTO.class));

        // When & Then
        mockMvc.perform(post("/api/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(createDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void updateTopic_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        TopicUpdateDTO updateDTO = new TopicUpdateDTO();
        updateDTO.setTitle("更新后的课题标题");
        updateDTO.setDescription("更新后的课题描述");
        updateDTO.setSource("更新来源");
        updateDTO.setType("理论研究");
        updateDTO.setNature("应用研究");
        updateDTO.setDifficulty(4);
        updateDTO.setWorkload(4);
        updateDTO.setMaxSelections(5);
        updateDTO.setStatus(1);
        
        doNothing().when(topicService).updateTopic(eq(1L), any(TopicUpdateDTO.class));

        // When & Then
        mockMvc.perform(put("/api/topics/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteTopic_ExistingTopic_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(topicService).deleteTopic(1L);

        // When & Then
        mockMvc.perform(delete("/api/topics/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getTopicPage_WithSearchParameters_ShouldReturnFilteredResults() throws Exception {
        // Given
        TopicPageQueryDTO queryDTO = new TopicPageQueryDTO();
        queryDTO.setTitle("Spring Boot");
        queryDTO.setTeacherId(1L);
        queryDTO.setStatus(1);
        
        when(topicService.getTopicPage(any(TopicPageQueryDTO.class))).thenReturn(mockTopicPage);

        // When & Then
        mockMvc.perform(get("/api/topics/page")
                .param("current", "1")
                .param("size", "10")
                .param("title", "Spring Boot")
                .param("teacherId", "1")
                .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].title").value("基于Spring Boot的毕业设计管理系统"));
    }

    @Test
    void createTopic_WithInvalidData_ShouldReturnValidationError() throws Exception {
        // Given
        TopicCreateDTO invalidDTO = new TopicCreateDTO();
        // 不设置必需字段

        // When & Then
        mockMvc.perform(post("/api/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopicById_NonExistingTopic_ShouldReturnError() throws Exception {
        // Given
        when(topicService.getTopicById(999L)).thenThrow(new RuntimeException("课题不存在"));

        // When & Then
        mockMvc.perform(get("/api/topics/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getTopicPage_ByStudentRole_ShouldReturnPublishedTopics() throws Exception {
        // Given - 学生只能看到已发布的课题
        TopicPageQueryDTO queryDTO = new TopicPageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        queryDTO.setStatus(1);
        
        when(topicService.getTopicPage(any(TopicPageQueryDTO.class))).thenReturn(mockTopicPage);

        // When & Then
        mockMvc.perform(get("/api/topics/page")
                .param("current", "1")
                .param("size", "10")
                .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].status").value(1));
    }

    @Test
    void getTopicPage_ByTeacherRole_ShouldReturnTeacherOwnTopics() throws Exception {
        // Given - 教师可以看到自己创建的课题
        TopicPageQueryDTO queryDTO = new TopicPageQueryDTO();
        queryDTO.setCurrent(1);
        queryDTO.setSize(10);
        queryDTO.setTeacherId(1L);
        
        when(topicService.getTopicPage(any(TopicPageQueryDTO.class))).thenReturn(mockTopicPage);

        // When & Then
        mockMvc.perform(get("/api/topics/page")
                .param("current", "1")
                .param("size", "10")
                .param("teacherId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].teacherId").value(1));
    }

    @Test
    void updateTopic_ChangingStatus_ShouldReturnSuccess() throws Exception {
        // Given
        TopicUpdateDTO updateDTO = new TopicUpdateDTO();
        updateDTO.setStatus(4);
        
        doNothing().when(topicService).updateTopic(eq(1L), any(TopicUpdateDTO.class));

        // When & Then
        mockMvc.perform(put("/api/topics/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.toJson(updateDTO)))
                .andExpect(status().isOk());
    }
}