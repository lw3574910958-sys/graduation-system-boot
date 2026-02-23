# ReviewStatus枚举canResubmit()方法使用优化说明

## 问题背景
ReviewStatus枚举中的`canResubmit()`方法一直未被使用，该方法用于判断文档是否可以重新提交审核（即被驳回的文档）。

## 优化内容

### 1. 添加重新提交文档功能
在DocumentService接口中添加了新的方法：
```java
/**
 * 重新提交被驳回的文档
 *
 * @param documentId 文档ID
 * @param userId 用户ID
 * @param newFile 新文件
 * @return 更新后的文档VO
 */
DocumentVO resubmitDocument(Long documentId, Long userId, MultipartFile newFile);
```

### 2. 在DocumentServiceImpl中实现核心逻辑
```java
@Override
@Transactional(rollbackFor = Exception.class)
public DocumentVO resubmitDocument(Long documentId, Long userId, MultipartFile newFile) {
    // 1. 获取文档信息和权限验证
    BizDocument document = getById(documentId);
    if (!document.getUserId().equals(userId)) {
        throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权限重新提交此文档");
    }
    
    // 2. 关键：使用canResubmit()方法验证状态
    ReviewStatus currentStatus = ReviewStatus.getByValue(document.getReviewStatus());
    if (currentStatus == null || !currentStatus.canResubmit()) {
        throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文档状态不允许重新提交");
    }
    
    // 3. 文件验证和上传
    // 4. 更新文档信息，重置为待审核状态
    document.setReviewStatus(ReviewStatus.PENDING.getValue());
    // ... 其他更新逻辑
    
    return convertToDocumentVO(document);
}
```

### 3. 业务流程说明
1. **状态验证**：只有被驳回(REJECTED)的文档才能重新提交
2. **权限检查**：只能由原文档上传者重新提交
3. **文件验证**：新文件必须符合类型和大小要求
4. **状态重置**：重新提交后文档状态重置为待审核(PENDING)
5. **数据更新**：清空之前的审核信息，更新上传时间和文件信息

## 技术要点

### 枚举方法的实际应用
```java
// 使用ReviewStatus枚举的canResubmit()方法
ReviewStatus currentStatus = ReviewStatus.getByValue(document.getReviewStatus());
if (currentStatus == null || !currentStatus.canResubmit()) {
    // 处理不允许重新提交的情况
}
```

### 状态流转设计
```
PENDING(0) ──审核──→ APPROVED(1)
    ↑                   ↓
    └─────重新提交────── REJECTED(2)
```

## 验证结果
✅ 编译通过
✅ ReviewStatus.canResubmit()方法得到实际使用
✅ 符合业务逻辑需求
✅ 保持了代码的一致性和可维护性

## 总结
通过添加文档重新提交功能，不仅解决了`canResubmit()`方法未使用的问题，还完善了文档管理的业务流程，使被驳回的文档能够有机会重新提交审核，提升了系统的用户体验和实用性。