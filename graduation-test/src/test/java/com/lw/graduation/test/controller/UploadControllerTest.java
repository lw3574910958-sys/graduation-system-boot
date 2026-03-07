package com.lw.graduation.test.controller;

import com.lw.graduation.api.controller.file.UploadController;
import com.lw.graduation.api.service.file.UnifiedFileUploadService;
import com.lw.graduation.api.vo.file.FileUploadResultVO;
import com.lw.graduation.test.config.WebMvcTestConfig;
import com.lw.graduation.test.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTestConfig
@WebMvcTest(UploadController.class)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UnifiedFileUploadService unifiedFileUploadService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // 准备模拟文件
        mockFile = new MockMultipartFile(
                "file",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello World".getBytes()
        );
    }

    @Test
    void uploadFile_WithValidFile_ShouldReturnSuccess() throws Exception {
        // Given
        String expectedPath = "/uploads/test-file.txt";
        when(unifiedFileUploadService.uploadFile(any(MultipartFile.class), any(String.class)))
                .thenReturn(new FileUploadResultVO());

        // When & Then
        mockMvc.perform(multipart("/api/upload")
                .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(expectedPath));
    }

    @Test
    void uploadFile_WithEmptyFile_ShouldReturnValidationError() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload")
                .file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_WithLargeFile_ShouldReturnValidationError() throws Exception {
        // Given
        byte[] largeContent = new byte[10 * 1024 * 1024 + 1]; // 超过10MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                largeContent
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload")
                .file(largeFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_WithoutFile_ShouldReturnValidationError() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/upload"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_ServiceThrowsException_ShouldReturnError() throws Exception {
        // Given
        when(unifiedFileUploadService.uploadFile(any(MultipartFile.class), any(String.class)))
                .thenThrow(new RuntimeException("文件存储失败"));

        // When & Then
        mockMvc.perform(multipart("/api/upload")
                .file(mockFile))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void uploadMultipleFiles_WithValidFiles_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "file1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Content 1".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "file2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Content 2".getBytes()
        );

        when(unifiedFileUploadService.uploadFile(any(MultipartFile.class), any(String.class)))
                .thenReturn(new FileUploadResultVO())
                .thenReturn(new FileUploadResultVO());

        // When & Then
        mockMvc.perform(multipart("/api/upload/multiple")
                .file(file1)
                .file(file2))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void uploadDocument_WithValidDocument_ShouldReturnSuccess() throws Exception {
        // Given
        String expectedPath = "/documents/thesis.pdf";
        when(unifiedFileUploadService.uploadDocument(any(MultipartFile.class), any(Long.class), any(Integer.class), any(Long.class)))
                .thenReturn(new FileUploadResultVO());

        // When & Then
        mockMvc.perform(multipart("/api/upload/document")
                .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(expectedPath));
    }

    @Test
    void uploadAvatar_WithValidImage_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image content".getBytes()
        );
        
        String expectedPath = "/avatars/user-avatar.jpg";
        when(unifiedFileUploadService.uploadAvatar(any(MultipartFile.class), any(Long.class)))
                .thenReturn(new FileUploadResultVO());

        // When & Then
        mockMvc.perform(multipart("/api/upload/avatar")
                .file(imageFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(expectedPath));
    }

    @Test
    void uploadFile_UnsupportedFileType_ShouldReturnValidationError() throws Exception {
        // Given
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "executable content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/upload")
                .file(unsupportedFile))
                .andExpect(status().isBadRequest());
    }
}