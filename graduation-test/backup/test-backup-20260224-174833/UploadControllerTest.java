package com.lw.graduation.test.controller;

import com.lw.graduation.api.controller.file.UploadController;
import com.lw.graduation.api.service.file.UnifiedFileUploadService;
import com.lw.graduation.api.vo.file.FileUploadResultVO;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestConfig
@WebMvcTest(UploadController.class)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnifiedFileUploadService unifiedFileUploadService;

    private FileUploadResultVO mockUploadResult;

    @BeforeEach
    void setUp() {
        // 准备模拟数据
        mockUploadResult = new FileUploadResultVO();
        mockUploadResult.setOriginalName("test-avatar.jpg");
        mockUploadResult.setFileSize(102400L);
        mockUploadResult.setContentType("image/jpeg");
        mockUploadResult.setStoredPath("avatar/1/2024/01/01/avatar_123456.jpg");
        mockUploadResult.setUrl("/files/avatar/1/2024/01/01/avatar_123456.jpg");
        mockUploadResult.setExtension("jpg");
        mockUploadResult.setUploadTime("2024-01-01 12:00:00");
    }

    @Test
    void uploadFile_WithValidData_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-document.pdf", 
                "application/pdf", 
                "PDF content".getBytes()
        );
        
        when(unifiedFileUploadService.uploadFile(any(MockMultipartFile.class), eq("document")))
                .thenReturn(mockUploadResult);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/upload/file")
                .file(file)
                .param("category", "document"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.originalName").value("test-avatar.jpg"))
                .andExpect(jsonPath("$.data.fileSize").value(102400))
                .andExpect(jsonPath("$.data.url").value("/files/avatar/1/2024/01/01/avatar_123456.jpg"));
    }

    @Test
    void uploadAvatar_WithValidImage_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile avatarFile = new MockMultipartFile(
                "file", 
                "avatar.jpg", 
                "image/jpeg", 
                "image content".getBytes()
        );
        
        when(unifiedFileUploadService.uploadAvatar(any(MockMultipartFile.class), eq(1L)))
                .thenReturn(mockUploadResult);

        // When & Then
        mockMvc.perform(multipart("/api/upload/avatar")
                .file(avatarFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.data.extension").value("jpg"));
    }

    @Test
    void uploadDocument_WithValidDocument_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile documentFile = new MockMultipartFile(
                "file", 
                "毕业论文.docx", 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                "document content".getBytes()
        );
        
        when(unifiedFileUploadService.uploadDocument(
                any(MockMultipartFile.class), eq(1L), eq(2), eq(1L)))
                .thenReturn(mockUploadResult);

        // When & Then
        mockMvc.perform(multipart("/api/upload/document")
                .file(documentFile)
                .param("topicId", "1")
                .param("fileType", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.originalName").value("test-avatar.jpg"));
    }

    @Test
    void uploadFile_EmptyFile_ShouldReturnValidationError() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        // When & Then
        mockMvc.perform(multipart("/api/upload/file")
                .file(emptyFile)
                .param("category", "document"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAvatar_InvalidFileType_ShouldReturnError() throws Exception {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", 
                "document.pdf", 
                "application/pdf", 
                "PDF content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/avatar")
                .file(invalidFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_LargeFile_ShouldReturnError() throws Exception {
        // Given - 创建一个大文件（超过限制）
        byte[] largeContent = new byte[51 * 1024 * 1024]; // 51MB，超过默�?0MB限制
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", 
                "large-file.zip", 
                "application/zip", 
                largeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/file")
                .file(largeFile)
                .param("category", "document"))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void uploadDocument_MissingParameters_ShouldReturnValidationError() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.docx", 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                "content".getBytes()
        );

        // When & Then - 缺少必需参数
        mockMvc.perform(multipart("/api/upload/document")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAvatar_UnsupportedImageFormat_ShouldReturnError() throws Exception {
        // Given
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file", 
                "image.bmp", 
                "image/bmp", 
                "bitmap content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload/avatar")
                .file(unsupportedFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_SuccessResponseStructure_ShouldBeCorrect() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.txt", 
                "text/plain", 
                "simple text".getBytes()
        );
        
        when(unifiedFileUploadService.uploadFile(any(MockMultipartFile.class), eq("general")))
                .thenReturn(mockUploadResult);

        // When & Then
        mockMvc.perform(multipart("/api/upload/file")
                .file(file)
                .param("category", "general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.originalName").exists())
                .andExpect(jsonPath("$.data.fileSize").exists())
                .andExpect(jsonPath("$.data.contentType").exists())
                .andExpect(jsonPath("$.data.storedPath").exists())
                .andExpect(jsonPath("$.data.url").exists());
    }

    @Test
    void uploadMultipleFiles_DifferentCategories_ShouldHandleEachCorrectly() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "file", 
                "photo.png", 
                "image/png", 
                "png content".getBytes()
        );
        
        MockMultipartFile docFile = new MockMultipartFile(
                "file", 
                "report.docx", 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                "doc content".getBytes()
        );

        FileUploadResultVO imageResult = new FileUploadResultVO();
        imageResult.setOriginalName("photo.png");
        imageResult.setContentType("image/png");
        imageResult.setStoredPath("images/photo_123.png");

        FileUploadResultVO docResult = new FileUploadResultVO();
        docResult.setOriginalName("report.docx");
        docResult.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        docResult.setStoredPath("documents/report_456.docx");

        when(unifiedFileUploadService.uploadFile(imageFile, "images")).thenReturn(imageResult);
        when(unifiedFileUploadService.uploadFile(docFile, "documents")).thenReturn(docResult);

        // When & Then - 测试图片上传
        mockMvc.perform(multipart("/api/upload/file")
                .file(imageFile)
                .param("category", "images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalName").value("photo.png"));

        // When & Then - 测试文档上传
        mockMvc.perform(multipart("/api/upload/file")
                .file(docFile)
                .param("category", "documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.originalName").value("report.docx"));
    }
}
