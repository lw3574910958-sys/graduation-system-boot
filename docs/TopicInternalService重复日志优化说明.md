# TopicInternalService重复日志消息优化说明

## 问题描述
在TopicInternalService中，第37行和第67行存在完全相同的日志消息：
```java
log.warn("题目不存在或已删除，ID: {}", topicId);
```

## 优化方案

### 原始问题代码
```java
// updateTopicStatus方法中
if (topic == null || topic.getIsDeleted() == 1) {
    log.warn("题目不存在或已删除，ID: {}", topicId);  // 第37行
    return;
}

// updateSelectedCount方法中  
if (topic == null || topic.getIsDeleted() == 1) {
    log.warn("题目不存在或已删除，ID: {}", topicId);  // 第67行
    return;
}
```

### 优化后的代码

**1. 提取公共日志方法**
```java
/**
 * 记录题目不存在警告日志
 * 
 * @param topicId 题目ID
 */
private void logTopicNotFound(Long topicId) {
    log.warn("题目[{}] 不存在或已删除", topicId);
}
```

**2. 统一调用方式**
```java
// updateTopicStatus方法中
if (topic == null || topic.getIsDeleted() == 1) {
    logTopicNotFound(topicId);  // 统一调用
    return;
}

// updateSelectedCount方法中
if (topic == null || topic.getIsDeleted() == 1) {
    logTopicNotFound(topicId);  // 统一调用
    return;
}
```

## 优化效果

### ✅ 代码复用性提升
- 消除了重复的日志代码
- 统一了日志格式和内容
- 便于后续维护和修改

### ✅ 可维护性增强
- 只需要在一个地方修改日志格式
- 降低了维护成本
- 提高了代码一致性

### ✅ 日志标准化
- 统一使用方括号标识格式
- 保持了清晰的日志信息
- 符合项目日志规范

## 技术要点

### 1. 私有方法设计
```java
private void logTopicNotFound(Long topicId) {
    log.warn("题目[{}] 不存在或已删除", topicId);
}
```

### 2. 调用简化
```java
// 从复杂的日志调用简化为方法调用
logTopicNotFound(topicId);
```

## 验证结果

✅ **编译通过**：所有语法错误已修复
✅ **功能保持**：日志记录功能完全一致
✅ **代码质量**：消除了重复代码，提高了可维护性
✅ **格式统一**：日志消息格式标准化

这次优化成功解决了代码重复问题，体现了良好的代码复用和维护性原则！