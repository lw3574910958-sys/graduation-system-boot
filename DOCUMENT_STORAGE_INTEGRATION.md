# 文档存储服务整合说明

## 整合概述

本次整合将原有的 `DocumentServiceImpl.java` 和 `LocalFileStorageServiceImpl.java` 进行了合理的架构分离和优化：

### 1. 架构设计原则

遵循**分层架构**和**单一职责原则**：
- **Infrastructure层**：负责纯粹的技术基础设施（文件存储）
- **Application层**：负责业务逻辑处理（文档管理）

### 2. 文件分布

#### 2.1 Infrastructure层文件
**路径：** `graduation-infrastructure/src/main/java/com/lw/graduation/infrastructure/storage/impl/LocalFileStorageServiceImpl.java`

**职责：**
- 实现 `FileStorageService` 接口
- 提供基础的文件存储功能
- 处理文件安全验证（类型、大小、路径遍历等）
- 生成安全的文件存储路径

**核心方法：**
```java
public String store(MultipartFile file, String category) throws IOException
private void validateFile(MultipartFile file)
```

#### 2.2 Application层文件  
**路径：** `graduation-document/src/main/java/com/lw/graduation/document/service/impl/DocumentServiceImpl.java`

**职责：**
- 实现 `DocumentService` 接口
- 处理文档业务逻辑（上传、下载、审核、删除）
- 调用 Infrastructure 层的文件存储服务
- 管理文档元数据和关联信息

**核心方法：**
```java
public DocumentVO uploadDocument(DocumentUploadDTO uploadDTO, Long userId) throws IOException
public InputStream downloadDocument(Long documentId, Long userId)
public void reviewDocument(DocumentReviewDTO reviewDTO, Long reviewerId)
public boolean deleteDocument(Long id, Long userId)
```

### 3. 依赖关系

```
graduation-document (业务层)
    ↓ 依赖
graduation-infrastructure (基础设施层)
    ↓ 提供
FileStorageService 接口实现
```

### 4. 主要改进

#### 4.1 解决循环依赖问题
- 移除了 infrastructure 对 api/common 模块的依赖
- 保持了合理的依赖方向：上层依赖下层

#### 4.2 职责分离
- 文件存储功能完全下沉到 infrastructure 层
- 文档业务逻辑保留在 application 层
- 通过接口进行松耦合调用

#### 4.3 扩展性提升
- FileStorageService 接口支持多种存储实现（本地、OSS、MinIO等）
- 文档服务可以灵活切换不同的文件存储策略

### 5. 使用示例

#### 5.1 文件上传流程
```java
// Controller 层调用
@PostMapping("/upload")
public Result<DocumentVO> upload(@RequestBody DocumentUploadDTO dto, 
                                @RequestAttribute Long userId) throws IOException {
    DocumentVO document = documentService.uploadDocument(dto, userId);
    return Result.success(document);
}

// Service 层实现
@Override
@Transactional(rollbackFor = Exception.class)
public DocumentVO uploadDocument(DocumentUploadDTO uploadDTO, Long userId) throws IOException {
    // 1. 业务验证
    validateUploadPermission(userId, uploadDTO.getTopicId());
    
    // 2. 调用基础设施层存储文件
    String folder = "documents/" + fileType.name().toLowerCase();
    String storedPath = fileStorageService.store(uploadDTO.getFile(), folder);
    
    // 3. 保存文档元数据
    BizDocument document = new BizDocument();
    document.setStoredPath(storedPath);
    // ... 其他属性设置
    
    save(document);
    return convertToDocumentVO(document);
}
```

### 6. 后续优化建议

#### 6.1 功能完善
- [ ] 在 `FileStorageService` 接口中添加 `delete()` 方法
- [ ] 实现 `downloadDocument()` 的文件流返回功能
- [ ] 添加文件访问权限控制

#### 6.2 性能优化
- [ ] 实现文件分片上传支持大文件
- [ ] 添加文件压缩和格式转换功能
- [ ] 实现文件缓存机制

#### 6.3 扩展性增强
- [ ] 支持云存储（阿里云OSS、腾讯云COS等）
- [ ] 实现存储策略动态切换
- [ ] 添加文件版本管理功能

### 7. 测试验证

项目已通过以下验证：
- ✅ Maven 编译成功
- ✅ 无循环依赖问题
- ✅ 架构层次清晰
- ✅ 职责分离明确

---
**整合完成时间：** 2026年2月16日  
**整合人员：** AI Assistant