package com.lw.graduation.test.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.controller.document.DocumentController;
import com.lw.graduation.api.dto.document.DocumentPageQueryDTO;
import com.lw.graduation.api.dto.document.DocumentReviewDTO;
import com.lw.graduation.api.dto.document.DocumentUploadDTO;
import com.lw.graduation.api.service.document.DocumentService;
import com.lw.graduation.api.vo.document.DocumentVO;
import com.lw.graduation.test.config.WebMvcTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestConfig
@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    private DocumentVO mockDocumentVO;
    private IPage<DocumentVO> mockDocumentPage;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockDocumentVO = new DocumentVO();
        mockDocumentVO.setId(1L);
        mockDocumentVO.setUserId(1L);
        mockDocumentVO.setUserName("张三");
        mockDocumentVO.setTopicId(1L);
        mockDocumentVO.setTopicTitle("基于Spring Boot的毕业设计管理系�?);
        mockDocumentVO.setFileType(2); // 毕业论文
        mockDocumentVO.setFileTypeDesc("毕业论文");
        mockDocumentVO.setOriginalFilename("毕业论文.docx");
        mockDocumentVO.setFileSize(1024000L);
        mockDocumentVO.setFileSizeDisplay("1.0 MB");
        mockDocumentVO.setFileExtension("docx");
        mockDocumentVO.setReviewStatus(0); // 待审
        mockDocumentVO.setReviewStatusDesc("待审");
        mockDocumentVO.setUploadedAt(LocalDateTime.now());
        mockDocumentVO.setCreatedAt(LocalDateTime.now());

        // 准备分页数据
        Page<DocumentVO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(mockDocumentVO));
        page.setTotal(1L);
        mockDocumentPage = page;
    }

    @Test
    void getDocumentPage_ShouldReturnDocumentPage() throws Exception {
        // Given
        DocumentPageQueryDTO queryDTO = new DocumentPageQueryDTO();
        when(documentService.getDocumentPage(any(DocumentPageQueryDTO.class))).thenReturn(mockDocumentPage);

        // When & Then
        mockMvc.perform(get("/api/documents/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.records[0].userName").value("张三"))
                .andExpect(jsonPath("$.data.records[0].topicTitle").value("基于Spring Boot的毕业设计管理系�?))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getDocumentById_ExistingDocument_ShouldReturnDocumentVO() throws Exception {
        // Given
        when(documentService.getDocumentById(1L)).thenReturn(mockDocumentVO);

        // When & Then
        mockMvc.perform(get("/api/documents/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.userName").value("张三"))
                .andExpect(jsonPath("$.data.originalFilename").value("毕业论文.docx"))
                .andExpect(jsonPath("$.data.fileSizeDisplay").value("1.0 MB"));
    }

    @Test
    void uploadDocument_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.docx", 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                "test content".getBytes()
        );
        
        DocumentUploadDTO uploadDTO = new DocumentUploadDTO();
        uploadDTO.setTopicId(1L);
        uploadDTO.setFileType(2);
        uploadDTO.setFile(file);
        
        when(documentService.uploadDocument(any(DocumentUploadDTO.class), eq(1L))).thenReturn(mockDocumentVO);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents/upload")
                .file(file)
                .param("topicId", "1")
                .param("fileType", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void reviewDocument_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        DocumentReviewDTO reviewDTO = new DocumentReviewDTO();
        reviewDTO.setDocumentId(1L);
        reviewDTO.setReviewStatus(1); // 通过
        reviewDTO.setFeedback("文档符合要求，通过审核");
        
        doNothing().when(documentService).reviewDocument(any(DocumentReviewDTO.class), eq(1L));

        // When & Then
        mockMvc.perform(post("/api/documents/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "documentId": 1,
                    "reviewStatus": 1,
                    "feedback": "文档符合要求，通过审核"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void deleteDocument_ExistingDocument_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(documentService).deleteDocument(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/documents/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void getDocumentPage_WithFilters_ShouldReturnFilteredResults() throws Exception {
        // Given
        DocumentPageQueryDTO queryDTO = new DocumentPageQueryDTO();
        queryDTO.setUserId(1L);
        queryDTO.setTopicId(1L);
        queryDTO.setFileType(2);
        queryDTO.setReviewStatus(1);
        
        when(documentService.getDocumentPage(any(DocumentPageQueryDTO.class))).thenReturn(mockDocumentPage);

        // When & Then
        mockMvc.perform(get("/api/documents/page")
                .param("current", "1")
                .param("size", "10")
                .param("userId", "1")
                .param("topicId", "1")
                .param("fileType", "2")
                .param("reviewStatus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].userName").value("张三"));
    }

    @Test
    void getDocumentById_NonExistingDocument_ShouldReturnError() throws Exception {
        // Given
        when(documentService.getDocumentById(999L)).thenThrow(new RuntimeException("文档不存�?));

        // When & Then
        mockMvc.perform(get("/api/documents/{id}", 999L))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void downloadDocument_ExistingDocument_ShouldReturnFile() throws Exception {
        // Given
        // 注意：文件下载测试需要特殊的mock处理，在实际测试中可能需要使用MockMvc的文件下载功�?
        
        // When & Then
        mockMvc.perform(get("/api/documents/{id}/download", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getDocumentPage_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Given
        Page<DocumentVO> page = new Page<>(2, 5);
        page.setRecords(Arrays.asList(mockDocumentVO));
        page.setTotal(15L);
        when(documentService.getDocumentPage(any(DocumentPageQueryDTO.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/documents/page")
                .param("current", "2")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.total").value(15));
    }
}
