# Java枚举与实体类开发规范

## 1. 枚举类设计规范

### 1.1 基本结构
```java
@Getter
@AllArgsConstructor
public enum StatusEnum {
    ITEM_NAME(value, "描述"),
    
    private final Integer value;
    private final String description;
    
    // 标准方法
    public static StatusEnum getByValue(Integer value) { ... }
    public static boolean isValid(Integer value) { ... }
}
```

### 1.2 必须方法
```java
public static StatusEnum getByValue(Integer value) {
    if (value == null) return null;
    for (StatusEnum item : values()) {
        if (item.value.equals(value)) {
            return item;
        }
    }
    return null;
}

public static boolean isValid(Integer value) {
    return getByValue(value) != null;
}
```

## 2. 实体类规范

### 2.1 状态检查统一实现
```java
public class BizEntity {
    @TableField("status")
    private Integer status;
    
    // 统一委托给枚举处理
    public boolean isFinalStatus() {
        StatusEnum statusEnum = StatusEnum.getByValue(this.status);
        return statusEnum != null && statusEnum.isFinalStatus();
    }
    
    public boolean canPublish() {
        StatusEnum statusEnum = StatusEnum.getByValue(this.status);
        return statusEnum != null && statusEnum.canPublish();
    }
}
```

### 2.2 禁止的做法
```java
// ❌ 禁止硬编码数字比较
public boolean isPublished() {
    return this.status == 1;
}

// ❌ 禁止重复实现业务逻辑
public boolean canPublish() {
    return this.status == 0;
}
```

## 3. 服务层使用规范

### 3.1 推荐用法
```java
// ✅ 使用实体类方法进行状态检查
if (notice.isFinalStatus()) {
    throw new BusinessException("已发布的通知不能直接删除");
}

if (notice.canPublish()) {
    // 执行发布操作
}

// ✅ 状态设置使用枚举常量
notice.setStatus(NoticeStatus.PUBLISHED.getValue());

// ✅ 查询条件使用枚举
wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getValue());
```

### 3.2 禁止用法
```java
// ❌ 禁止直接使用枚举进行业务逻辑
NoticeStatus status = NoticeStatus.getByValue(notice.getStatus());
if (status == null || !status.canPublish()) { ... }

// ❌ 禁止硬编码状态值
if (notice.getStatus() == 1) { ... }
```

## 4. 包结构规范

### 4.1 枚举类组织
```
enums/
├── notice/           # NoticeStatus, NoticeType
├── document/         # FileType
├── grade/            # GradeLevel
├── status/           # ReviewStatus, SelectionStatus, TopicStatus
├── user/             # AccountStatus, Gender, UserType
└── permission/       # SystemRole
```

### 4.2 实体类组织
```
entity/
├── notice/           # BizNotice
├── document/         # BizDocument
├── selection/        # BizSelection
├── topic/            # BizTopic
├── grade/            # BizGrade
└── user/             # SysUser, BizStudent, BizTeacher, BizAdmin
```

## 5. 命名规范

### 5.1 枚举类命名
- 状态类：`XXXStatus` (NoticeStatus, TopicStatus)
- 类型类：`XXXType` (NoticeType, FileType)
- 等级类：`XXXLevel` (GradeLevel)
- 角色类：`XXXRole` (AdminRole, SystemRole)

### 5.2 枚举项命名
- 使用大写：`ITEM_NAME`
- 语义清晰：`PENDING_REVIEW` 而不是 `PENDING_REV`

### 5.3 方法命名
- 查询方法：`getByValue()`, `isValid()`
- 判断方法：`isXXX()`, `canXXX()`, `hasXXX()`

## 6. 最佳实践

### 6.1 空值处理
```java
// ✅ 正确处理null值
public boolean isApproved() {
    ReviewStatus status = ReviewStatus.getByValue(this.reviewStatus);
    return status == ReviewStatus.APPROVED;
}

// ✅ 特殊场景明确处理null
public boolean isEditable() {
    ReviewStatus status = ReviewStatus.getByValue(this.reviewStatus);
    return status != null && status.canEdit();
}
```

### 6.2 性能优化
```java
// ✅ 缓存常用查找
private static final Map<Integer, NoticeStatus> CACHE = 
    Arrays.stream(NoticeStatus.values())
          .collect(Collectors.toMap(NoticeStatus::getValue, Function.identity()));
```

## 7. 代码审查清单

- [ ] 枚举类实现标准方法`getByValue()`和`isValid()`
- [ ] 实体类状态检查委托给枚举处理
- [ ] 服务层使用实体类方法而非直接操作枚举
- [ ] 状态设置使用枚举常量
- [ ] 查询条件使用枚举值
- [ ] 包结构符合规范
- [ ] 命名清晰规范
- [ ] 空值处理恰当
- [ ] 有单元测试覆盖

---
**版本**: 1.0  **适用范围**: graduation-system项目