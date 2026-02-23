# BizNotice与NoticeStatus统一引用规范

## 问题背景

在通知模块的服务实现中，同时存在对BizNotice实体类和NoticeStatus枚举的引用，容易出现以下问题：

1. **引用混乱**：同一业务场景下混用两种不同的API
2. **维护困难**：需要同时维护两套相似的功能方法
3. **学习成本**：开发人员需要熟悉两套API的使用场景

## 统一引用规范

### ✅ 强制要求：统一使用BizNotice实体类方法

#### 理由：
1. **封装性好**：实体类方法封装了枚举调用逻辑
2. **语义清晰**：提供更高层次的业务语义表达
3. **减少依赖**：服务层无需直接依赖枚举类
4. **易于扩展**：业务逻辑变更只需修改实体类
5. **一致性**：避免团队成员使用不统一的API

### 🚫 严格禁止的做法：

1. **直接使用NoticeStatus进行业务逻辑判断**：
```java
// ❌ 严格禁止：直接使用枚举进行业务逻辑判断
NoticeStatus status = NoticeStatus.getByValue(notice.getStatus());
if (status == NoticeStatus.PUBLISHED || status == NoticeStatus.WITHDRAWN) {
    // 业务逻辑
}

// ❌ 严格禁止：混合使用两种API
if (notice.isPublished() && NoticeStatus.getByValue(notice.getStatus()).canWithdraw()) {
    // 业务逻辑
}
```

2. **在业务逻辑中直接操作枚举内部逻辑**：
```java
// ❌ 严格禁止：业务逻辑中直接操作枚举
if (NoticeStatus.getByValue(notice.getStatus()).canPublish()) {
    // 业务处理
}
```

## 推荐的引用方式

### 1. 状态检查统一使用BizNotice方法

```java
// ✅ 推荐：使用实体类方法
if (notice.isFinalStatus()) {
    throw new BusinessException("已发布的通知不能直接删除");
}

if (notice.canPublish()) {
    // 执行发布操作
}

if (notice.canWithdraw()) {
    // 执行撤回操作
}

if (notice.isReadOnly()) {
    throw new BusinessException("只有草稿状态的通知才能编辑");
}
```

### 2. 状态设置使用NoticeStatus枚举常量

```java
// ✅ 推荐：状态设置时使用枚举常量
notice.setStatus(NoticeStatus.PUBLISHED.getValue());
notice.setStatus(NoticeStatus.WITHDRAWN.getValue());
notice.setStatus(NoticeStatus.DRAFT.getValue());
```

### 3. 查询条件使用NoticeStatus枚举

```java
// ✅ 推荐：构建查询条件时使用枚举
LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getValue());
```

## 具体应用场景规范

### 场景1：业务逻辑判断
```java
// ✅ 统一使用BizNotice实体方法
@Override
public void updateNotice(Long id, NoticeUpdateDTO updateDTO, Long updaterId) {
    BizNotice notice = getById(id);
    
    // 状态检查统一使用实体类方法
    if (notice.isWithdrawn()) {
        throw new BusinessException("已撤回的通知不能编辑");
    }
    
    if (notice.isReadOnly()) {
        throw new BusinessException("只有草稿状态的通知才能编辑");
    }
    
    // 业务处理...
}
```

### 场景2：状态变更操作
```java
// ✅ 状态设置使用枚举常量
@Override
public void publishNotice(Long id, Long publisherId) {
    BizNotice notice = getById(id);
    
    if (!notice.canPublish()) {
        throw new BusinessException("只有草稿状态的通知才能发布");
    }
    
    // 使用枚举常量设置状态
    notice.setStatus(NoticeStatus.PUBLISHED.getValue());
    notice.setPublishedAt(LocalDateTime.now());
    
    updateById(notice);
}
```

### 场景3：查询构建
```java
// ✅ 查询条件使用枚举
@Override
public List<NoticeVO> getPublishedNotices() {
    LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getValue())
           .eq(BizNotice::getIsDeleted, 0)
           .orderByDesc(BizNotice::getPublishedAt);
    
    return list(wrapper).stream()
            .filter(BizNotice::isEffective)
            .map(this::convertToNoticeVO)
            .collect(Collectors.toList());
}
```

## 引用优先级排序

1. **强制要求**：BizNotice实体类方法（状态检查、业务判断）
2. **允许使用**：NoticeStatus枚举常量（状态设置、查询条件）
3. **严格禁止**：直接使用NoticeStatus枚举进行复杂业务逻辑

## 验证清单

在代码审查时检查以下要点：

- [ ] ✅ 状态检查必须统一使用BizNotice实体方法
- [ ] ✅ 状态设置可以使用NoticeStatus枚举常量
- [ ] ✅ 查询条件可以使用NoticeStatus枚举
- [ ] ❌ 严禁在同一方法内混用两种API进行业务逻辑
- [ ] ❌ 严禁在业务逻辑中直接操作NoticeStatus枚举
- [ ] ✅ 业务逻辑必须通过BizNotice实体类方法表达清楚

## 总结

通过建立统一的引用规范，我们可以：
- ✅ 降低维护成本
- ✅ 提高代码一致性
- ✅ 减少学习成本
- ✅ 增强代码可读性
- ✅ 便于后续扩展

建议团队严格按照此规范进行开发，确保代码质量和一致性。