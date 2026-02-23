# sys.sql 数据库脚本更新说明

## 更新内容

### 新增 biz_notice 表
在 `sql/sys.sql` 中添加了通知公告表的完整定义，与 `BizNotice.java` 实体类完全匹配。

## 表结构详情

```sql
CREATE TABLE `biz_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '通知类型: 1-系统通知, 2-公告, 3-提醒',
  `priority` TINYINT NOT NULL DEFAULT 2 COMMENT '优先级: 1-低, 2-中, 3-高',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID(sys_user.id)',
  `published_at` DATETIME(3) NULL DEFAULT NULL COMMENT '发布时间',
  `start_time` DATETIME(3) NULL DEFAULT NULL COMMENT '生效开始时间',
  `end_time` DATETIME(3) NULL DEFAULT NULL COMMENT '生效结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已撤回',
  `is_sticky` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
  `read_count` INT NOT NULL DEFAULT 0 COMMENT '阅读次数',
  `target_scope` TINYINT NOT NULL DEFAULT 0 COMMENT '目标范围: 0-全体, 1-学生, 2-教师, 3-管理员',
  `attachment_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '附件URL',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_publisher` (`publisher_id`),
  KEY `idx_type` (`type`),
  KEY `idx_priority` (`priority`),
  KEY `idx_status` (`status`),
  KEY `idx_target_scope` (`target_scope`),
  KEY `idx_published_at` (`published_at`),
  CONSTRAINT `fk_notice_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';
```

## 字段映射验证

| 实体类字段 | 数据库字段 | 数据类型 | 状态 |
|-----------|-----------|----------|------|
| id | id | BIGINT | ✅ 匹配 |
| title | title | VARCHAR(200) | ✅ 匹配 |
| content | content | TEXT | ✅ 匹配 |
| type | type | TINYINT | ✅ 匹配 |
| priority | priority | TINYINT | ✅ 匹配 |
| publisherId | publisher_id | BIGINT | ✅ 匹配 |
| publishedAt | published_at | DATETIME(3) | ✅ 匹配 |
| startTime | start_time | DATETIME(3) | ✅ 匹配 |
| endTime | end_time | DATETIME(3) | ✅ 匹配 |
| status | status | TINYINT | ✅ 匹配 |
| isSticky | is_sticky | TINYINT | ✅ 匹配 |
| readCount | read_count | INT | ✅ 匹配 |
| targetScope | target_scope | TINYINT | ✅ 匹配 |
| attachmentUrl | attachment_url | VARCHAR(500) | ✅ 匹配 |
| createdAt | created_at | DATETIME(3) | ✅ 匹配 |
| updatedAt | updated_at | DATETIME(3) | ✅ 匹配 |
| isDeleted | is_deleted | TINYINT(1) | ✅ 匹配 |

## 索引和约束

- **主键**: id (自增)
- **外键**: publisher_id 引用 sys_user.id (级联删除)
- **索引**: 
  - idx_publisher (发布者ID)
  - idx_type (通知类型)
  - idx_priority (优先级)
  - idx_status (状态)
  - idx_target_scope (目标范围)
  - idx_published_at (发布时间)

## 更新影响

此次更新使数据库表结构与实体类完全同步，确保了：
1. ORM映射的完整性
2. 数据持久化的正确性
3. 业务逻辑的一致性
4. 系统功能的完整性

建议在下次数据库部署时执行此更新脚本。
