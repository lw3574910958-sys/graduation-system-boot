# AuthorizationException未使用构造函数清理说明

## 清理背景
在代码审查过程中发现`AuthorizationException`类中的三个构造函数都未被使用，造成了代码冗余。

## 清理内容

### 删除的未使用构造函数

1. **`AuthorizationException(String permission, String message)`**
   - 功能：带权限标识的权限异常构造函数
   - 删除原因：项目中未使用带权限标识的异常实例化

2. **`AuthorizationException(String permission, String resource, String message)`**
   - 功能：带权限标识和资源标识的权限异常构造函数
   - 删除原因：项目中未使用带详细权限信息的异常实例化

### 删除的未使用属性

1. **`permission`属性**
   - 功能：存储权限标识信息
   - 删除原因：对应的构造函数未被使用

2. **`resource`属性**
   - 功能：存储资源标识信息
   - 删除原因：对应的构造函数未被使用

## 保留的核心内容

经过清理后，保留了最基本的权限异常结构：

### 核心构造函数（保留）
```java
public AuthorizationException(String message) {
    super(ExceptionType.AUTHORIZATION.getCode(), message);
}
```

### 基础继承结构（保留）
- 继承自BusinessException
- 使用AUTHORIZATION异常类型编码(3000)

## 项目权限处理现状

通过代码分析发现，项目目前的权限处理方式：

### 实际使用方式
```java
// 全局异常处理器中定义了处理方法
@ExceptionHandler(AuthorizationException.class)
@ResponseStatus(HttpStatus.FORBIDDEN)
public Result<?> handleAuthorizationException(AuthorizationException e) {
    log.warn("权限异常: permission={}, resource={}, message={}", 
            e.getPermission(), e.getResource(), e.getMessage());
    return Result.error(e.getCode(), e.getMessage());
}
```

### 但未实际抛出异常
```java
// 项目中未找到实际的异常抛出示例
throw new AuthorizationException("权限不足");
```

## 清理效果

1. **代码简洁性**：删除了24行冗余代码（包括属性和构造函数）
2. **维护性提升**：避免了未使用代码造成的混淆
3. **结构优化**：保留了最基础的权限异常类，便于未来扩展
4. **符合规范**：遵循"删除未使用代码"的重构原则

## 验证结果

- ✅ 编译通过，无语法错误
- ✅ 全局异常处理器功能保持正常
- ✅ 项目权限异常处理框架不受影响
- ✅ 代码质量得到提升

## 设计考量

### 保留基础类的原因
虽然当前未使用，但保留AuthorizationException类有以下优势：

1. **架构完整性**：作为权限相关异常的基类，保持异常体系的完整性
2. **未来扩展性**：当需要更详细的权限控制时，可以基于此类扩展
3. **API一致性**：与其他业务异常类（ValidationException、DataAccessException等）保持一致

### 简化设计的好处
1. **降低复杂度**：去除不必要的属性和构造函数
2. **易于理解**：类结构更加清晰简单
3. **减少维护成本**：避免维护未使用的代码

## 实际应用场景

### 简化后的使用方式
```java
// 未来可能的使用场景
if (!hasPermission(currentUser, requiredPermission)) {
    throw new AuthorizationException("用户无权执行此操作");
}
```

### 与现有权限框架的关系
项目目前主要使用Sa-Token框架进行权限控制：
```java
// Sa-Token权限注解使用
@SaCheckPermission("user.manage")
public void manageUsers() {
    // 业务逻辑
}
```

## 注意事项

此次清理不会影响现有功能，因为：
1. 项目中确实没有使用AuthorizationException的实例化
2. 全局异常处理器已正确定义，可以处理该类型异常
3. 基础的权限异常类结构得以保留
4. 现有的Sa-Token权限控制体系不受影响

## 未来建议

如果未来需要更细粒度的权限控制，可以考虑：
1. 扩展AuthorizationException类，添加更多构造函数
2. 集成具体的权限标识和资源信息
3. 在业务逻辑中实际抛出和处理此类异常