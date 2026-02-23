# TopicServiceImpl重复日志优化说明

## 问题描述
在`handleSelectionReviewed`方法中，第401行和419行存在完全相同的日志消息：
```java
log.info("题目[{}] 操作完成: 所有申请处理完毕，恢复为开放状态", topicId);
```

## 优化方案

### 原始问题代码
```java
// 审核通过分支
if (selectionApproved && currentStatus == TopicStatus.REVIEWING) {
    // ... 查询逻辑 ...
    if (pendingCount == 0) {
        updateTopicStatus(topicId, TopicStatus.OPEN, false);
        log.info("题目[{}] 操作完成: 所有申请处理完毕，恢复为开放状态", topicId);
    }
}

// 审核驳回分支  
else if (!selectionApproved && currentStatus == TopicStatus.REVIEWING) {
    // ... 查询逻辑 ...
    if (pendingCount == 0) {
        updateTopicStatus(topicId, TopicStatus.OPEN, false);
        log.info("题目[{}] 操作完成: 所有申请处理完毕，恢复为开放状态", topicId);
    }
}
```

### 优化后的代码

**1. 提取公共方法**
```java
/**
 * 处理题目状态恢复逻辑
 * 
 * @param topicId 题目ID
 * @param selectionFilter 选题过滤条件
 */
private void handleTopicStatusRecovery(Long topicId, java.util.function.Predicate<BizSelection> selectionFilter) {
    // 检查是否还有符合条件的申请
    LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(BizSelection::getTopicId, topicId)
           .eq(BizSelection::getIsDeleted, 0);
    
    long pendingCount = bizSelectionMapper.selectList(wrapper).stream()
            .filter(selectionFilter)
            .count();
    
    // 如果没有待处理的申请，恢复为开放状态
    if (pendingCount == 0) {
        updateTopicStatus(topicId, TopicStatus.OPEN, false);
        log.info("题目[{}] 操作完成: 所有申请处理完毕，恢复为开放状态", topicId);
    }
}
```

**2. 简化主方法**
```java
@Transactional(rollbackFor = Exception.class)
public void handleSelectionReviewed(Long topicId, boolean selectionApproved) {
    BizTopic topic = getById(topicId);
    if (topic == null || topic.getIsDeleted() == 1) {
        return;
    }
    
    TopicStatus currentStatus = TopicStatus.getByValue(topic.getStatus());
    
    // 如果题目当前是审核中状态
    if (currentStatus == TopicStatus.REVIEWING) {
        // 根据审核结果选择不同的过滤条件
        java.util.function.Predicate<BizSelection> filter = selectionApproved ? 
            selection -> selection.isPendingReview() || selection.isApproved() :
            BizSelection::isPendingReview;
            
        handleTopicStatusRecovery(topicId, filter);
    }
}
```

## 优化效果

### ✅ 代码质量提升
- **消除重复代码**：减少了约20行重复代码
- **提高可维护性**：统一的逻辑处理，便于后续修改
- **增强可读性**：主方法逻辑更加清晰简洁

### ✅ 功能保持完整
- 业务逻辑完全一致
- 审核通过和驳回的处理逻辑正确区分
- 日志记录保持原有格式

### ✅ 设计优化
- **单一职责原则**：`handleTopicStatusRecovery`专门处理状态恢复逻辑
- **参数化设计**：通过Predicate参数灵活控制过滤条件
- **代码复用**：相同的恢复逻辑得到统一处理

## 验证结果

✅ **编译通过**：所有语法错误已修复
✅ **逻辑正确**：审核通过和驳回的处理逻辑保持正确
✅ **日志统一**：消除了重复的日志消息
✅ **性能保持**：查询逻辑和性能无变化

这次优化成功解决了代码重复问题，同时提升了代码的整体质量和可维护性！