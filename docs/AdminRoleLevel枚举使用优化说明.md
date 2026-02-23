# AdminRoleLevel枚举使用优化说明

## 问题背景
发现`AdminRoleLevel`枚举类定义后从未被使用，造成了代码冗余。

## 分析结果
通过代码分析发现：
1. 数据库表`biz_admin`中存在`role_level`字段，值为0(系统管理员)和1(院系管理员)
2. 这与`AdminRoleLevel`枚举定义完全匹配
3. 但在`BizAdmin`实体类中，该字段仍为Integer类型，未使用枚举

## 优化方案
选择在实体类中使用该枚举，而不是删除它，因为：
- 数据库结构已为此做好准备
- 权限管理是系统的必要功能
- 使用枚举可以提高类型安全性和代码可读性

## 实施内容

### 1. 添加枚举导入
在`BizAdmin.java`中添加：
```java
import com.lw.graduation.domain.enums.permission.AdminRoleLevel;
```

### 2. 新增枚举相关方法
```java
/**
 * 获取管理员角色等级枚举
 *
 * @return AdminRoleLevel枚举
 */
public AdminRoleLevel getRoleLevelEnum() {
    return AdminRoleLevel.values()[this.roleLevel];
}

/**
 * 检查是否为系统管理员
 *
 * @return 系统管理员返回true
 */
public boolean isSystemAdmin() {
    return this.roleLevel != null && this.roleLevel.equals(AdminRoleLevel.SYSTEM_ADMIN.getValue());
}

/**
 * 检查是否为院系管理员
 *
 * @return 院系管理员返回true
 */
public boolean isDeptAdmin() {
    return this.roleLevel != null && this.roleLevel.equals(AdminRoleLevel.DEPT_ADMIN.getValue());
}
```

## 优化效果

1. **消除未使用警告**：AdminRoleLevel枚举现在有了明确的使用场景
2. **提高类型安全性**：使用枚举替代魔术数字
3. **增强代码可读性**：通过方法名明确表达业务意图
4. **便于未来扩展**：为权限管理功能奠定基础

## 验证结果
- ✅ 编译通过，无语法错误
- ✅ 枚举类现在有明确用途
- ✅ 保持了与数据库结构的一致性
- ✅ 为后续权限功能开发做好准备

## 后续建议
建议在管理员相关服务中逐步使用这些新添加的方法，充分发挥枚举的优势。