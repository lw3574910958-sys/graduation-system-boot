# AccountStatus枚举使用优化说明

## 问题背景
AccountStatus枚举定义了用户账户的启用和禁用状态，但一直未被使用，存在硬编码状态值的问题。

## 优化内容

### 1. 完善AccountStatus枚举
为AccountStatus枚举添加了必要的方法：

```java
public enum AccountStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    // 新增方法
    public static AccountStatus getByValue(Integer value) { ... }
    public static boolean isValid(Integer value) { ... }
    public boolean isEnabled() { ... }
    public boolean isDisabled() { ... }
}
```

### 2. 在AuthService中使用AccountStatus

#### 登录验证中使用枚举
**优化前：**
```java
if (user.getStatus() != 1) {
    throw new BusinessException(ResponseCode.ACCOUNT_DISABLED);
}
```

**优化后：**
```java
AccountStatus accountStatus = AccountStatus.getByValue(user.getStatus());
if (accountStatus == AccountStatus.DISABLED) {
    throw new BusinessException(ResponseCode.ACCOUNT_DISABLED);
}
```

### 3. 在UserService中使用AccountStatus

#### 用户创建时使用枚举
**优化前：**
```java
user.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1); // 默认启用
```

**优化后：**
```java
user.setStatus(createDTO.getStatus() != null ? 
    createDTO.getStatus() : AccountStatus.ENABLED.getValue()); // 默认启用
```

#### 用户状态更新时增加验证
```java
if (updateDTO.getStatus() != null) {
    // 验证状态值是否有效
    if (!AccountStatus.isValid(updateDTO.getStatus())) {
        throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "无效的账户状态值");
    }
    updateUser.setStatus(updateDTO.getStatus());
}
```

### 4. 新增账户状态管理功能

#### 启用/禁用用户账户
```java
@Override
@Transactional(rollbackFor = Exception.class)
public void enableUser(Long id) {
    updateUserStatus(id, AccountStatus.ENABLED);
}

@Override
@Transactional(rollbackFor = Exception.class)
public void disableUser(Long id) {
    updateUserStatus(id, AccountStatus.DISABLED);
}
```

#### 状态更新核心逻辑
```java
private void updateUserStatus(Long id, AccountStatus status) {
    // 1. 检查用户是否存在
    SysUser user = sysUserMapper.selectById(id);
    if (user == null) {
        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }
    
    // 2. 检查当前状态是否与目标状态相同
    AccountStatus currentStatus = AccountStatus.getByValue(user.getStatus());
    if (currentStatus == status) {
        String action = status.isEnabled() ? "启用" : "禁用";
        throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
            String.format("账户已经是%s状态", action));
    }
    
    // 3. 更新状态
    SysUser updateUser = new SysUser();
    updateUser.setId(id);
    updateUser.setStatus(status.getValue());
    updateUser.setUpdatedAt(LocalDateTime.now());
    
    sysUserMapper.updateById(updateUser);
    
    // 4. 清除缓存
    clearUserCache(id);
}
```

## 业务流程说明

### 账户状态管理流程
```
DISABLED(0) ←→ ENABLED(1)
   禁用          启用
```

### 状态变更规则
1. **启用账户**：只有禁用状态的账户可以被启用
2. **禁用账户**：只有启用状态的账户可以被禁用
3. **重复操作**：不允许对已经是目标状态的账户进行操作

### 权限控制
- 管理员可以启用/禁用任意用户账户
- 教师和学生无权限修改他人账户状态
- 用户不能自我禁用账户

## 技术要点

### 枚举使用的最佳实践
1. **类型安全**：避免硬编码数字，使用枚举常量
2. **集中管理**：状态定义和验证逻辑集中在枚举中
3. **语义清晰**：`AccountStatus.DISABLED`比`status == 0`更易理解
4. **扩展性强**：如需添加中间状态，只需修改枚举定义

### 状态验证机制
```java
// 输入验证
if (!AccountStatus.isValid(updateDTO.getStatus())) {
    throw new BusinessException("无效的账户状态值");
}

// 业务逻辑验证
AccountStatus currentStatus = AccountStatus.getByValue(user.getStatus());
if (currentStatus == targetStatus) {
    throw new BusinessException("账户已经是目标状态");
}
```

## 验证结果
✅ 编译通过  
✅ AccountStatus枚举得到充分使用  
✅ 消除了硬编码状态值  
✅ 符合业务逻辑需求  
✅ 保持了代码的一致性和可维护性  

## 总结
通过本次优化，不仅解决了AccountStatus枚举未使用的问题，还：
- 增强了账户状态管理功能
- 提高了代码的类型安全性
- 统一了状态处理逻辑
- 为后续的用户管理功能扩展奠定了良好基础