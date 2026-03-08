-- ====================================
-- 修复学生表和教师表字段的 NULL 约束
-- 允许 department_id、major、class_name、title 为 NULL
-- ====================================

-- 1. 修改学生表字段，允许 NULL 值
ALTER TABLE `biz_student` 
MODIFY COLUMN `department_id` BIGINT NULL DEFAULT NULL COMMENT '所属院系 ID(NULL 表示无院系)',
MODIFY COLUMN `major` VARCHAR(100) NULL DEFAULT NULL COMMENT '专业',
MODIFY COLUMN `class_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '班级';

-- 2. 修改教师表字段，允许 NULL 值
ALTER TABLE `biz_teacher` 
MODIFY COLUMN `department_id` BIGINT NULL DEFAULT NULL COMMENT '所属院系 ID(NULL 表示无院系)',
MODIFY COLUMN `title` VARCHAR(50) NULL DEFAULT NULL COMMENT '职称 (教授/副教授等)';

-- ====================================
-- 说明：
-- 1. 所有表的主键使用 ASSIGN_ID（雪花算法），不存在 id=0 的情况
-- 2. 前端传递 departmentId: 0 表示"无院系"
-- 3. 后端代码会将 0 转换为 null 再存入数据库
-- 4. 管理员的 role_level 根据 department_id 是否为空自动判断：
--    - department_id IS NULL → role_level = 0 (系统管理员)
--    - department_id IS NOT NULL → role_level = 1 (院系管理员)
-- ====================================
