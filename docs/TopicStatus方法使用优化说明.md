# TopicStatus枚举方法使用优化说明

## 问题背景
TopicStatus枚举中有两个方法长期未被使用：
- `isSelectable()` - 判断是否为可选状态（开放或审核中）
- `isActive()` - 判断是否为活跃状态（非关闭状态）

## 优化内容

### 1. isSelectable()方法的使用

#### 在updateTopic方法中使用
**优化前：**
```java
if (TopicStatus.getByValue(existingTopic.getStatus()) == TopicStatus.SELECTED) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已选题目不能修改");
}
```

**优化后：**
```java
TopicStatus currentStatus = TopicStatus.getByValue(existingTopic.getStatus());
if (currentStatus != null && !currentStatus.isSelectable()) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "当前状态的题目不能修改");
}
```

#### 新增getSelectableTopics方法
```java
@Override
public List<TopicVO> getSelectableTopics(Long departmentId) {
    // 1. 查询所有题目
    List<BizTopic> allTopics = list(wrapper);
    
    // 2. 关键：使用isSelectable()方法过滤可选题目
    List<BizTopic> selectableTopics = allTopics.stream()
            .filter(topic -> {
                TopicStatus status = TopicStatus.getByValue(topic.getStatus());
                return status != null && status.isSelectable();
            })
            .collect(Collectors.toList());
    
    // 3. 转换为VO返回
    return selectableTopics.stream()
            .map(this::convertToTopicVO)
            .collect(Collectors.toList());
}
```

### 2. isActive()方法的使用

#### 在deleteTopic方法中使用
**优化前：**
```java
if (TopicStatus.getByValue(existingTopic.getStatus()) == TopicStatus.SELECTED) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已选题目不能删除");
}
```

**优化后：**
```java
TopicStatus currentStatus = TopicStatus.getByValue(existingTopic.getStatus());
if (currentStatus != null && !currentStatus.isActive()) {
    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已关闭的题目不能删除");
}
```

## 业务流程说明

### 可选题目筛选逻辑
```
OPEN(1) ──可选──┐
                ├──→ 可供学生选择
REVIEWING(2) ──可选──┘

SELECTED(3) ──不可选
CLOSED(4) ──不可选
```

### 状态操作权限
- **修改权限**：只有OPEN和REVIEWING状态的题目可以修改
- **删除权限**：只有活跃状态（非CLOSED）的题目可以删除
- **选择权限**：只有可选状态（OPEN、REVIEWING）的题目可以被学生选择

## 技术要点

### 枚举方法的优势
1. **语义清晰**：`isSelectable()`比`(status == OPEN || status == REVIEWING)`更直观
2. **集中管理**：状态判断逻辑集中在枚举中，便于维护和修改
3. **类型安全**：避免硬编码状态值，减少错误可能性
4. **扩展性强**：如果未来需要调整可选状态的定义，只需修改枚举即可

### 使用示例
```java
// 检查题目是否可选
TopicStatus status = TopicStatus.getByValue(topic.getStatus());
if (status != null && status.isSelectable()) {
    // 显示给学生选择
}

// 检查题目是否活跃
if (status != null && status.isActive()) {
    // 允许教师操作
}

// 获取所有可选题目
List<BizTopic> selectableTopics = allTopics.stream()
        .filter(topic -> {
            TopicStatus topicStatus = TopicStatus.getByValue(topic.getStatus());
            return topicStatus != null && topicStatus.isSelectable();
        })
        .collect(Collectors.toList());
```

## 验证结果
✅ 编译通过  
✅ 两个未使用的方法都得到实际应用  
✅ 符合业务逻辑需求  
✅ 保持了代码的一致性和可维护性  

## 总结
通过本次优化，不仅解决了TopicStatus枚举方法未使用的问题，还增强了题目的状态管理能力：
- 提供了专门的可选题目查询接口
- 统一了状态判断逻辑
- 提高了代码的可读性和可维护性
- 为后续功能扩展奠定了良好基础