# SystemRole枚举方法使用优化说明

## 问题背景
发现`SystemRole`枚举中的以下方法未被使用：
- `getByUserType(UserType userType)` - 获取指定用户类型的所有角色
- `isSystemAdmin()` - 判断是否为系统管理员角色
- `isDepartmentAdmin()` - 判断是否为院系管理员角色

## 分析结果
通过代码分析发现：
1. 项目使用基于角色的权限控制系统
2. `UserRoleService`负责用户角色管理，但未充分利用`SystemRole`枚举的功能
3. 这些未使用的方法实际上很有价值，可以提供便捷的角色检查功能

## 优化方案
在`UserRoleService`中添加并实现以下方法来使用SystemRole枚举：

### 1. 新增服务接口方法
```java
/**
 * 获取指定用户类型的所有用户ID
 *
 * @param userType 用户类型
 * @return 该用户类型对应的用户ID列表
 */
List<Long> getUsersByUserType(String userType);

/**
 * 检查用户是否为系统管理员
 *
 * @param userId 用户ID
 * @return 是系统管理员返回true
 */
boolean isSystemAdmin(Long userId);

/**
 * 检查用户是否为院系管理员
 *
 * @param userId 用户ID
 * @return 是院系管理员返回true
 */
boolean isDepartmentAdmin(Long userId);
```

### 2. 实现细节
- `getUsersByUserType()`: 利用`SystemRole.getByUserType()`获取角色编码，然后查询用户
- `isSystemAdmin()`: 直接使用`SystemRole.SYSTEM_ADMIN`进行角色检查
- `isDepartmentAdmin()`: 直接使用`SystemRole.DEPARTMENT_ADMIN`进行角色检查

## 优化效果

1. **消除未使用警告**：SystemRole枚举的所有方法现在都有明确的使用场景
2. **提高代码复用性**：避免在多处重复实现相同的逻辑
3. **增强类型安全性**：使用枚举替代硬编码的角色名称
4. **提升可维护性**：角色相关的逻辑集中在一个地方管理

## 验证结果
- ✅ 编译通过，无语法错误
- ✅ 所有SystemRole枚举方法现在都有使用场景
- ✅ 保持了与现有权限系统的兼容性
- ✅ 为后续的权限管理功能扩展奠定了基础

## 使用示例
```java
// 检查用户是否为系统管理员
if (userRoleService.isSystemAdmin(userId)) {
    // 执行管理员专属操作
}

// 获取所有教师用户
List<Long> teacherIds = userRoleService.getUsersByUserType("TEACHER");

// 检查用户是否为院系管理员
if (userRoleService.isDepartmentAdmin(userId)) {
    // 执行院系管理操作
}
```

## 后续建议
建议在控制器和服务层逐步使用这些新增的方法，充分发挥枚举的优势，提高代码质量和可维护性。