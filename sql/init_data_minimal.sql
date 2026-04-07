-- graduation_system_minimal_init.sql
-- 高校毕业设计论文管理系统 - 最小化初始化脚本
-- 仅创建一个系统管理员账户，其他表为空
-- 适用于快速启动和基础测试

USE graduation_system;

-- ============================
-- 零、清空现有数据
-- ============================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE sys_log;
TRUNCATE TABLE biz_grade;
TRUNCATE TABLE biz_selection;
TRUNCATE TABLE biz_document;
TRUNCATE TABLE biz_notice;
TRUNCATE TABLE biz_topic;
TRUNCATE TABLE biz_student;
TRUNCATE TABLE biz_teacher;
TRUNCATE TABLE biz_admin;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_department;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================
-- 一、插入系统管理员账户
-- ============================

-- 默认密码: Admin@123
-- BCrypt哈希: $2b$10$04KBIA8bMrHqA3BDPUVRZexAjgkiuyho84w5S89BbAEbGAKyWIub2

INSERT INTO sys_user (
    id, 
    username, 
    password, 
    real_name, 
    user_type, 
    status, 
    avatar, 
    last_login_at, 
    last_login_ip, 
    login_fail_count, 
    locked_until, 
    created_at, 
    updated_at, 
    is_deleted
) VALUES (
    1800000000000000001,
    'sys_admin',
    '$2b$10$04KBIA8bMrHqA3BDPUVRZexAjgkiuyho84w5S89BbAEbGAKyWIub2',
    '系统管理员',
    'system_admin',
    1,
    NULL,
    NULL,
    NULL,
    0,
    NULL,
    NOW(3),
    NOW(3),
    0
);

-- ============================
-- 二、插入管理员业务信息
-- ============================

INSERT INTO biz_admin (
    id, 
    user_id, 
    admin_id, 
    department_id, 
    role_level, 
    phone, 
    email, 
    created_at, 
    updated_at, 
    is_deleted
) VALUES (
    1900000000000000001,
    1800000000000000001,
    'ADMIN_001',
    NULL,
    0,
    '13800138000',
    'admin@university.edu.cn',
    NOW(3),
    NOW(3),
    0
);

-- ============================
-- 三、验证数据
-- ============================

SELECT '=== 初始化完成 ===' AS info;
SELECT '系统管理员账户' AS account_type, username, real_name, user_type, status 
FROM sys_user 
WHERE id = 1800000000000000001;

SELECT '管理员业务信息' AS biz_type, admin_id, phone, email 
FROM biz_admin 
WHERE id = 1900000000000000001;

SELECT '数据统计' AS summary,
    (SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0) AS users,
    (SELECT COUNT(*) FROM biz_admin WHERE is_deleted = 0) AS admins,
    (SELECT COUNT(*) FROM biz_teacher WHERE is_deleted = 0) AS teachers,
    (SELECT COUNT(*) FROM biz_student WHERE is_deleted = 0) AS students,
    (SELECT COUNT(*) FROM biz_topic WHERE is_deleted = 0) AS topics,
    (SELECT COUNT(*) FROM biz_selection WHERE is_deleted = 0) AS selections,
    (SELECT COUNT(*) FROM biz_document WHERE is_deleted = 0) AS documents,
    (SELECT COUNT(*) FROM biz_grade WHERE is_deleted = 0) AS grades,
    (SELECT COUNT(*) FROM biz_notice WHERE is_deleted = 0) AS notices;

-- ============================
-- 使用说明
-- ============================
-- 登录账户: sys_admin
-- 登录密码: Admin@123
-- 用户类型: system_admin
-- 雪花ID: 1800000000000000001
-- ============================
