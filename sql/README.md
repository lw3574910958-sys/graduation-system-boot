# 数据库初始化说明

## 文件说明

### 1. sys.sql
- **用途**: 建表脚本，包含所有表结构定义
- **执行顺序**: 必须首先执行
- **注意**: 如果表已存在，会先删除再创建

### 2. init_data.sql
- **用途**: 大规模测试数据初始化脚本（564KB，2509行）
- **生成方式**: 由Python脚本自动生成
- **数据量**: 
  - 院系: 10个
  - 用户: 500个（系统管理员10 + 院系管理员50 + 教师140 + 学生300）
  - 题目: 600个（DRAFT 60 + REVIEWING 60 + OPEN 300 + CLOSED 180）
  - 选题: 200个（PENDING_REVIEW 40 + APPROVED 60 + REJECTED 40 + CONFIRMED 60）
  - 文档: 400个（PROPOSAL 150 + MIDTERM 150 + THESIS 100）
  - 成绩: 200个（PROPOSAL 60 + MIDTERM 60 + THESIS 40 + COMPOSITE 40）
  - 公告: 20个（DRAFT 2 + PUBLISHED 15 + WITHDRAWN 2 + SCHEDULED 1）

### 3. init_data_minimal.sql
- **用途**: 最小化初始化脚本（仅1个系统管理员）
- **适用场景**: 快速启动、基础功能测试、生产环境初始部署
- **数据内容**: 
  - 系统管理员: 1个（sys_admin / Admin@123）
  - 其他表: 全部为空
- **文件大小**: 约3KB

### 4. generate_init_data.py
- **用途**: Python脚本，用于生成init_data.sql
- **使用方法**: `python generate_init_data.py`
- **修改配置**: 可调整各表的数据量和ID范围

### 5. verify_init_data.sql
- **用途**: 数据完整性验证脚本
- **功能**: 检查所有外键关联、唯一约束、业务规则
- **执行时机**: 执行init_data.sql后运行

## 执行步骤

### 方案一：最小化初始化（推荐首次部署）
```sql
-- 1. 执行建表脚本
source sys.sql;

-- 2. 执行最小化初始化（仅1个管理员）
source init_data_minimal.sql;

-- 3. 登录后通过系统创建其他数据
```

### 方案二：大规模测试数据初始化
```sql
-- 1. 执行建表脚本
source sys.sql;

-- 2. 执行数据初始化脚本（大量测试数据）
source init_data.sql;

-- 3. 验证数据完整性
source verify_init_data.sql;
```

### 重新初始化（清空所有数据）
```sql
-- 两个脚本都包含TRUNCATE语句，直接执行即可
source init_data_minimal.sql;  -- 或
source init_data.sql;
```

## 雪花ID规则

| 数据类型 | ID范围 | 示例 |
|---------|--------|------|
| 院系 | 1700000000000000001 ~ 010 | 1700000000000000001 |
| 系统管理员 | 1800000000000000001 ~ 010 | 1800000000000000001 |
| 院系管理员 | 1800000000000000011 ~ 060 | 1800000000000000011 |
| 教师 | 1800000000000000061 ~ 200 | 1800000000000000061 |
| 学生 | 1800000000000000201 ~ 500 | 1800000000000000201 |
| 管理员业务 | 1900000000000000001 ~ 060 | 1900000000000000001 |
| 教师业务 | 1900000000000000061 ~ 200 | 1900000000000000061 |
| 学生业务 | 1900000000000000201 ~ 500 | 1900000000000000201 |
| 题目 | 2000000000000000001 ~ 600 | 2000000000000000001 |
| 选题 | 2100000000000000001 ~ 200 | 2100000000000000001 |
| 文档 | 2200000000000000001 ~ 400 | 2200000000000000001 |
| 成绩 | 2300000000000000001 ~ 200 | 2300000000000000001 |
| 公告 | 2400000000000000001 ~ 020 | 2400000000000000001 |

## 默认密码

所有用户的统一密码为: **Admin@123**

BCrypt哈希值: `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi`

## 外键关联关系

```
sys_department (院系)
    ├── biz_admin.department_id
    ├── biz_teacher.department_id
    ├── biz_student.department_id
    ├── biz_topic.department_id
    └── biz_notice.department_id

sys_user (系统用户)
    ├── biz_admin.user_id
    ├── biz_teacher.user_id
    ├── biz_student.user_id
    ├── biz_document.user_id
    ├── biz_notice.publisher_id
    └── biz_topic.reviewer_id

biz_teacher (教师业务)
    ├── biz_topic.teacher_id
    ├── biz_selection.reviewer_id
    ├── biz_document.reviewer_id
    └── biz_grade.grader_id

biz_student (学生业务)
    ├── biz_selection.student_id
    ├── biz_grade.student_id
    └── biz_document.user_id (通过sys_user关联)

biz_topic (题目)
    ├── biz_selection.topic_id
    ├── biz_document.topic_id
    └── biz_grade.topic_id
```

## 业务规则验证

### 1. 题目状态分布
- DRAFT (0): 60个 - 教师草稿，未提交审核
- REVIEWING (1): 60个 - 待管理员审核
- OPEN (2): 300个 - 已通过审核，学生可选
- CLOSED (3): 180个 - 已满员或手动关闭

### 2. 选题状态分布
- PENDING_REVIEW (0): 40个 - 待教师审核
- APPROVED (1): 60个 - 教师审核通过，待学生确认
- REJECTED (2): 40个 - 教师审核驳回
- CONFIRMED (3): 60个 - 学生已确认选题

### 3. 文档审核状态
- PENDING (0): 100个 - 待教师审核
- APPROVED (1): 200个 - 审核通过
- REJECTED (2): 100个 - 审核驳回

### 4. 成绩类型分布
- PROPOSAL (0): 60个 - 开题成绩
- MIDTERM (1): 60个 - 中期成绩
- THESIS (2): 40个 - 论文成绩
- COMPOSITE (3): 40个 - 综合成绩

### 5. 公告状态分布
- DRAFT (0): 2个 - 草稿
- PUBLISHED (1): 15个 - 已发布
- WITHDRAWN (2): 2个 - 已撤回
- SCHEDULED (0): 1个 - 定时发布（start_time > now）

## 注意事项

1. **字符集**: 确保数据库使用 `utf8mb4_unicode_ci` 字符集
2. **时区**: 所有时间字段使用当前系统时间（NOW(3)）
3. **外键检查**: 插入数据前会关闭外键检查（SET FOREIGN_KEY_CHECKS = 0）
4. **TRUNCATE**: 每次执行都会清空所有表数据
5. **日志表**: sys_log表暂不插入数据（根据需求除外）

## 常见问题

### Q1: 执行init_data.sql时报错"Foreign key constraint fails"
**A**: 确保按顺序执行：先sys.sql，再init_data.sql。init_data.sql已包含关闭外键检查的语句。

### Q2: 如何修改数据量？
**A**: 编辑generate_init_data.py，修改顶部的COUNT常量，然后重新运行脚本。

### Q3: 如何验证数据是否正确？
**A**: 执行verify_init_data.sql，查看所有验证项是否都返回0或预期值。

### Q4: 雪花ID是否可以自定义？
**A**: 可以。修改generate_init_data.py中的ID_START常量即可。

## 技术支持

如有问题，请检查：
1. MySQL版本 >= 8.0
2. 数据库字符集为utf8mb4
3. 有足够的磁盘空间（约1MB SQL文件）
4. 执行权限足够

---
生成时间: 2026-04-08
文件大小: 564KB
总记录数: 2509行SQL语句
