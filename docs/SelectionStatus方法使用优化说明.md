# SelectionStatus枚举方法使用优化说明

## 问题背景
SelectionStatus枚举中有三个方法长期未被使用：
- `isFinalStatus()` - 判断是否为最终状态（通过或驳回）
- `canResubmit()` - 判断是否可以重新提交
- `isActive()` - 判断是否为活跃状态（可以继续流程）

## 优化内容

### 1. isActive()方法的使用
在`applySelection`方法中替换原有的状态检查逻辑：

**优化前：**
```java
if (existing.isPendingReview() || existing.isApproved()) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "您已提交过该题目的申请，请勿重复申请");
}
```

**优化后：**
```java
SelectionStatus existingStatus = SelectionStatus.getByValue(existing.getStatus());
if (existingStatus != null && existingStatus.isActive()) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "您已提交过该题目的申请，请勿重复申请");
}
```

### 2. isFinalStatus()方法的使用
在`reviewSelection`方法中替换原有的状态检查逻辑：

**优化前：**
```java
if (selection.isApproved() || selection.isRejected()) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "选题状态不允许审核");
}
```

**优化后：**
```java
SelectionStatus currentStatus = SelectionStatus.getByValue(selection.getStatus());
if (currentStatus != null && currentStatus.isFinalStatus()) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "选题状态不允许审核");
}
```

### 3. canResubmit()方法的使用
新增了`resubmitSelection`方法，专门处理被驳回选题的重新申请：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public SelectionVO resubmitSelection(Long selectionId, Long studentId, String applyReason) {
    // 1. 获取原选题信息和权限验证
    BizSelection originalSelection = getById(selectionId);
    if (!originalSelection.getStudentId().equals(studentId)) {
        throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权重新申请他人选题");
    }
    
    // 2. 关键：使用canResubmit()方法验证状态
    SelectionStatus originalStatus = SelectionStatus.getByValue(originalSelection.getStatus());
    if (originalStatus == null || !originalStatus.canResubmit()) {
        throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该选题状态不允许重新申请");
    }
    
    // 3. 创建新的选题申请记录
    BizSelection newSelection = new BizSelection();
    newSelection.setStatus(SelectionStatus.PENDING_REVIEW.getValue()); // 重新设置为待审核状态
    
    // ... 其他业务逻辑
    
    return convertToSelectionVO(newSelection);
}
```

## 业务流程说明

### 重新申请流程
1. **状态验证**：只有被驳回(REJECTED)的选题才能重新申请
2. **权限检查**：只能由原申请人重新申请
3. **题目验证**：原题目必须仍处于开放状态
4. **次数限制**：同一学生对同一题目的申请次数不超过3次
5. **状态重置**：重新申请后状态重置为待审核(PENDING_REVIEW)

### 状态流转设计
```
PENDING_REVIEW(0) ──审核通过──→ APPROVED(1) ──学生确认──→ CONFIRMED(3)
     ↓                              ↑
   审核驳回                         │
     ↓                              │
  REJECTED(2) ──重新申请───────────┘
```

## 技术要点

### 枚举方法的优势
1. **语义清晰**：`isActive()`比`(status == PENDING || status == APPROVED)`更直观
2. **集中管理**：状态判断逻辑集中在枚举中，便于维护
3. **类型安全**：避免硬编码状态值，减少错误可能性

### 使用示例
```java
// 检查选题是否活跃（可以继续流程）
SelectionStatus status = SelectionStatus.getByValue(selection.getStatus());
if (status != null && status.isActive()) {
    // 处理活跃状态的选题
}

// 检查是否为最终状态
if (status != null && status.isFinalStatus()) {
    // 处理已完结的选题
}

// 检查是否可以重新提交
if (status != null && status.canResubmit()) {
    // 允许重新申请
}
```

## 验证结果
✅ 编译通过  
✅ 三个未使用的方法都得到实际应用  
✅ 符合业务逻辑需求  
✅ 保持了代码的一致性和可维护性  

## 总结
通过本次优化，不仅解决了SelectionStatus枚举方法未使用的问题，还增强了选题管理系统的功能完整性，使被驳回的选题能够有机会重新申请，提升了系统的用户体验和实用性。