# 初始化脚本选择指南

## 📋 快速对比

| 特性 | init_data_minimal.sql | init_data.sql |
|------|----------------------|---------------|
| **文件大小** | 3KB | 564KB |
| **执行速度** | < 1秒 | 5-10秒 |
| **数据量** | 1个管理员 | 2000+条记录 |
| **适用场景** | 生产部署、快速启动 | 开发测试、功能演示 |
| **包含院系** | ❌ 无 | ✅ 10个 |
| **包含教师** | ❌ 无 | ✅ 140个 |
| **包含学生** | ❌ 无 | ✅ 300个 |
| **包含题目** | ❌ 无 | ✅ 600个 |
| **包含选题** | ❌ 无 | ✅ 200个 |
| **包含文档** | ❌ 无 | ✅ 400个 |
| **包含成绩** | ❌ 无 | ✅ 200个 |
| **包含公告** | ❌ 无 | ✅ 20个 |

## 🎯 使用建议

### 选择 init_data_minimal.sql 如果：

✅ **生产环境首次部署**
- 从零开始，通过系统界面逐步创建数据
- 保证数据的真实性和准确性

✅ **快速验证系统功能**
- 只需登录验证基础功能
- 测试权限控制、菜单显示等

✅ **CI/CD自动化测试**
- 每次构建需要干净的初始状态
- 减少测试数据准备时间

✅ **演示环境搭建**
- 现场演示时快速初始化
- 避免大量测试数据干扰

### 选择 init_data.sql 如果：

✅ **开发环境**
- 需要大量测试数据验证功能
- 测试分页、搜索、筛选等功能

✅ **性能测试**
- 测试大数据量下的系统表现
- 验证查询优化效果

✅ **功能演示**
- 展示完整的业务流程
- 体现系统的实际应用场景

✅ **学习研究**
- 理解业务逻辑和数据关系
- 查看各种状态的数据样例

## 🚀 快速开始

### 最小化初始化（推荐）

```bash
# Windows PowerShell
cd d:\Project\myapps\graduation-system\graduation-system-boot\sql
mysql -u root -p graduation_system < sys.sql
mysql -u root -p graduation_system < init_data_minimal.sql
```

```bash
# Linux/Mac
cd /path/to/graduation-system-boot/sql
mysql -u root -p graduation_system < sys.sql
mysql -u root -p graduation_system < init_data_minimal.sql
```

**登录后：**
- 用户名: `sys_admin`
- 密码: `Admin@123`
- 角色: 系统管理员

### 大规模数据初始化

```bash
# Windows PowerShell
cd d:\Project\myapps\graduation-system\graduation-system-boot\sql
mysql -u root -p graduation_system < sys.sql
mysql -u root -p graduation_system < init_data.sql
mysql -u root -p graduation_system < verify_init_data.sql
```

**可用账户示例：**
- 系统管理员: `sys_admin_001` ~ `sys_admin_010`
- 院系管理员: `dept_admin_cs_001` ~ `dept_admin_ce_005`
- 教师: `teacher_cs_001` ~ `teacher_ce_014`
- 学生: `2022001` ~ `2022300`
- 统一密码: `Admin@123`

## 🔄 切换方案

### 从最小化切换到大规模数据

```sql
-- 直接执行大规模初始化脚本（会自动清空现有数据）
source init_data.sql;
```

### 从大规模数据切换到最小化

```sql
-- 直接执行最小化初始化脚本（会自动清空现有数据）
source init_data_minimal.sql;
```

## ⚠️ 注意事项

1. **两个脚本都包含TRUNCATE语句**，执行前会清空所有表数据
2. **不要在生产环境使用init_data.sql**，除非确实需要测试数据
3. **执行顺序**: 必须先执行 `sys.sql` 建表，再执行初始化脚本
4. **字符集**: 确保数据库使用 `utf8mb4_unicode_ci`
5. **权限**: 需要MySQL的TRUNCATE和INSERT权限

## 📊 数据恢复

如果误删了数据，可以重新执行对应的初始化脚本：

```sql
-- 恢复到最小化状态
source init_data_minimal.sql;

-- 或恢复到测试数据状态
source init_data.sql;
```

## 💡 最佳实践

### 开发阶段
```
1. 首次 setup: sys.sql + init_data.sql
2. 日常开发: 继续使用测试数据
3. 功能测试: 利用丰富的测试数据
```

### 测试阶段
```
1. 单元测试: init_data_minimal.sql（干净环境）
2. 集成测试: init_data.sql（完整场景）
3. 性能测试: init_data.sql + 额外数据生成
```

### 生产部署
```
1. 首次部署: sys.sql + init_data_minimal.sql
2. 数据录入: 通过系统界面逐步添加
3. 数据迁移: 从旧系统导入真实数据
```

---

**最后更新**: 2026-04-08  
**维护者**: 开发团队
