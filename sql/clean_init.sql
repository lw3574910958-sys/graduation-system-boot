-- graduation_system_clean_init.sql
-- 清空并重新初始化数据库脚本
-- 支持在表存在数据的情况下先清空再重新插入

USE graduation_system;

-- 设置时区
SET time_zone = '+08:00';

-- ==================== 1. 清空所有表数据 (按外键依赖反向顺序) ====================
-- 先清空无外键依赖的日志表
TRUNCATE TABLE sys_log;

-- 清空成绩表 (依赖学生、课题、用户)
TRUNCATE TABLE biz_grade;

-- 清空文档表 (依赖用户、课题)
TRUNCATE TABLE biz_document;

-- 清空选题表 (依赖学生、课题)
TRUNCATE TABLE biz_selection;

-- 清空课题表 (依赖教师)
TRUNCATE TABLE biz_topic;

-- 清空业务表 (依赖用户和院系)
TRUNCATE TABLE biz_admin;
TRUNCATE TABLE biz_teacher;
TRUNCATE TABLE biz_student;

-- 清空用户表 (依赖院系)
TRUNCATE TABLE sys_user;

-- 清空院系表 (无外键依赖)
TRUNCATE TABLE sys_department;

-- ==================== 2. 插入院系数据 (无外键依赖) ====================
INSERT INTO sys_department (id, code, name, created_at, updated_at) VALUES
(1, 'CS001', '计算机科学与技术学院', NOW(3), NOW(3)),
(2, 'EE001', '电子信息工程学院', NOW(3), NOW(3)),
(3, 'ME001', '机械工程学院', NOW(3), NOW(3)),
(4, 'MA001', '数学与应用数学学院', NOW(3), NOW(3)),
(5, 'EN001', '外国语学院', NOW(3), NOW(3));

-- ==================== 3. 插入用户数据 (无外键依赖) ====================
-- 现有用户数据 (保持原ID不变)
INSERT INTO sys_user (id, username, password, real_name, user_type, status, created_at, updated_at) VALUES
(2011317294235017217, 'admin', '{bcrypt}$2a$10$SG8SLCUzw8nAS/sD.xcYa.Qu0Nvt6XZ.JtpQUypfAEuvuQ/fvi/N6', '系统管理员', 'admin', 1, '2026-01-14 13:59:46.071', '2026-02-13 21:32:50.060'),
(2011317642420969473, 'student', '{bcrypt}$2a$10$l9WAyGvuGCrEbXPJvreFCubrV9afr/Wx.L/9nK7KqPP78yi9AHM5e', '学生用户', 'student', 1, '2026-01-14 14:01:09.088', '2026-02-13 21:24:03.392'),
(2011317884608471042, 'teacher', '{bcrypt}$2a$10$61NhQEeLs9Ouz8t567krreFBWcmzoqQjRTwC51YehoKjva7RWJHUK', '教师用户', 'teacher', 1, '2026-01-14 14:02:06.831', '2026-02-13 20:58:01.145');

-- 新增用户数据
INSERT INTO sys_user (id, username, password, real_name, user_type, status, created_at, updated_at) VALUES
(2011317884608471043, 'teacher002', '{bcrypt}$2a$10$61NhQEeLs9Ouz8t567krreFBWcmzoqQjRTwC51YehoKjva7RWJHUK', '李老师', 'teacher', 1, NOW(3), NOW(3)),
(2011317884608471044, 'teacher003', '{bcrypt}$2a$10$61NhQEeLs9Ouz8t567krreFBWcmzoqQjRTwC51YehoKjva7RWJHUK', '王老师', 'teacher', 1, NOW(3), NOW(3)),
(2011317642420969474, 'student002', '{bcrypt}$2a$10$l9WAyGvuGCrEbXPJvreFCubrV9afr/Wx.L/9nK7KqPP78yi9AHM5e', '张同学', 'student', 1, NOW(3), NOW(3)),
(2011317642420969475, 'student003', '{bcrypt}$2a$10$l9WAyGvuGCrEbXPJvreFCubrV9afr/Wx.L/9nK7KqPP78yi9AHM5e', '李同学', 'student', 1, NOW(3), NOW(3)),
(2011317294235017218, 'dept_admin_cs', '{bcrypt}$2a$10$SG8SLCUzw8nAS/sD.xcYa.Qu0Nvt6XZ.JtpQUypfAEuvuQ/fvi/N6', '计算机学院管理员', 'admin', 1, NOW(3), NOW(3));

-- ==================== 4. 插入业务数据 ====================
-- 管理员业务数据
INSERT INTO biz_admin (id, user_id, admin_id, department_id, role_level, phone, email, created_at, updated_at) VALUES
(1, 2011317294235017217, 'ADMIN001', NULL, 0, '13800138000', 'admin@university.edu.cn', NOW(3), NOW(3)),
(2, 2011317294235017218, 'ADMIN_CS001', 1, 1, '13800138010', 'admin_cs@university.edu.cn', NOW(3), NOW(3));

-- 教师业务数据
INSERT INTO biz_teacher (id, user_id, teacher_id, department_id, gender, title, phone, email, created_at, updated_at) VALUES
(1, 2011317884608471042, 'T2024001', 1, 1, '副教授', '13800138001', 'teacher@cs.university.edu.cn', NOW(3), NOW(3)),
(2, 2011317884608471043, 'T2024002', 1, 0, '讲师', '13800138002', 'li@cs.university.edu.cn', NOW(3), NOW(3)),
(3, 2011317884608471044, 'T2024003', 2, 1, '教授', '13800138003', 'wang@ee.university.edu.cn', NOW(3), NOW(3));

-- 学生业务数据
INSERT INTO biz_student (id, user_id, student_id, department_id, gender, major, class_name, phone, email, created_at, updated_at) VALUES
(1, 2011317642420969473, '2024001001', 1, 1, '计算机科学与技术', '计科2401班', '13900139001', 'student@stu.university.edu.cn', NOW(3), NOW(3)),
(2, 2011317642420969474, '2024001002', 1, 0, '软件工程', '软工2401班', '13900139002', 'zhang@stu.university.edu.cn', NOW(3), NOW(3)),
(3, 2011317642420969475, '2024002001', 2, 1, '电子信息工程', '电信2401班', '13900139003', 'li@stu.university.edu.cn', NOW(3), NOW(3));

-- ==================== 5. 插入课题数据 ====================
INSERT INTO biz_topic (id, title, description, teacher_id, status, created_at, updated_at) VALUES
(1, '基于Spring Boot的高校毕业设计管理系统设计与实现', '设计并实现一个完整的高校毕业设计管理系统，包括选题、文档管理、成绩评定等功能模块。', 1, 0, NOW(3), NOW(3)),
(2, '人工智能在图像识别中的应用研究', '研究深度学习算法在图像识别领域的应用，实现一个图像分类系统。', 1, 0, NOW(3), NOW(3)),
(3, '物联网智能家居控制系统设计', '基于物联网技术设计智能家居控制系统，实现设备远程控制和自动化管理。', 2, 0, NOW(3), NOW(3)),
(4, '基于区块链的数据安全存储系统', '研究区块链技术在数据安全存储方面的应用，设计分布式存储解决方案。', 3, 0, NOW(3), NOW(3));

-- ==================== 6. 插入选题数据 ====================
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, created_at, updated_at) VALUES
(1, 1, 1, '基于Spring Boot的高校毕业设计管理系统设计与实现', 1, NOW(3), NOW(3)),
(2, 2, 2, '人工智能在图像识别中的应用研究', 0, NOW(3), NOW(3)),
(3, 3, 4, '基于区块链的数据安全存储系统', 1, NOW(3), NOW(3));

-- ==================== 7. 插入文档数据 ====================
INSERT INTO biz_document (id, user_id, topic_id, file_type, original_filename, stored_path, file_size, review_status, uploaded_at, created_at, updated_at) VALUES
(1, 2011317642420969473, 1, 0, '开题报告_学生.docx', '/uploads/documents/2024/01/开题报告_学生.docx', 1024000, 1, NOW(3), NOW(3), NOW(3)),
(2, 2011317642420969473, 1, 1, '中期报告_学生.pdf', '/uploads/documents/2024/01/中期报告_学生.pdf', 2048000, 0, NOW(3), NOW(3), NOW(3));

-- ==================== 8. 插入成绩数据 ====================
INSERT INTO biz_grade (id, student_id, topic_id, score, grader_id, comment, graded_at, created_at, updated_at) VALUES
(1, 1, 1, 85.50, 2011317884608471042, '系统功能完整，界面友好，文档规范。', NOW(3), NOW(3), NOW(3)),
(2, 3, 4, 78.25, 2011317884608471044, '基本功能实现，但还需完善细节。', NOW(3), NOW(3), NOW(3));

-- ==================== 9. 插入系统日志数据 ====================
INSERT INTO sys_log (id, user_id, username, user_type, module, operation, business_id, status, ip_address, duration_ms, created_at) VALUES
(1, 2011317294235017217, 'admin', 'admin', 'user', '用户登录', NULL, 1, '127.0.0.1', 150, NOW(3)),
(2, 2011317884608471042, 'teacher', 'teacher', 'topic', '创建课题', 1, 1, '127.0.0.1', 200, NOW(3)),
(3, 2011317642420969473, 'student', 'student', 'selection', '提交选题申请', 1, 1, '127.0.0.1', 180, NOW(3));

-- ==================== 初始化完成提示 ====================
SELECT '数据库清空并重新初始化完成！' AS message;
SELECT '所有表数据已清空并重新插入' AS info;
SELECT '可以使用以下账号进行测试:' AS test_accounts;
SELECT 'admin/admin123 - 系统管理员' AS admin_account;
SELECT 'teacher/teacher123 - 教师账号' AS teacher_account;
SELECT 'student/student123 - 学生账号' AS student_account;