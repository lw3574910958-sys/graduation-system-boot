-- graduation_system_clean_init.sql
-- 清空并重新初始化数据库脚本
-- 支持在表存在数据的情况下先清空再重新插入
-- 严格按照外键依赖关系的反向顺序操作

USE graduation_system;

-- 设置时区
SET time_zone = '+08:00';

-- ==================== 1. 清空所有表数据 (严格按外键依赖反向顺序) ====================
-- 注意：必须按照外键依赖的反向顺序清空，避免外键约束冲突

-- 第一层：无外键依赖的表
TRUNCATE TABLE sys_log;

-- 第二层：依赖第一层表的表
TRUNCATE TABLE biz_grade;
TRUNCATE TABLE biz_document;
TRUNCATE TABLE biz_selection;

-- 第三层：依赖第二层表的表
TRUNCATE TABLE biz_topic;

-- 第四层：依赖第三层和其他基础表的表
TRUNCATE TABLE biz_admin;
TRUNCATE TABLE biz_teacher;
TRUNCATE TABLE biz_student;
TRUNCATE TABLE sys_user_role;

-- 第五层：最基础的表
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_department;

-- ==================== 2. 插入院系数据 (无外键依赖) ====================
INSERT INTO sys_department (id, code, name, created_at, updated_at) VALUES
(1, 'CS001', '计算机科学与技术学院', NOW(3), NOW(3)),
(2, 'EE001', '电子信息工程学院', NOW(3), NOW(3)),
(3, 'ME001', '机械工程学院', NOW(3), NOW(3)),
(4, 'MA001', '数学与应用数学学院', NOW(3), NOW(3)),
(5, 'EN001', '外国语学院', NOW(3), NOW(3));

-- ==================== 3. 插入用户数据 ====================
-- 系统管理员
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, login_fail_count, locked_until, created_at, updated_at) VALUES
(2011317294235017217, 'admin', '{bcrypt}$2a$10$SG8SLCUzw8nAS/sD.xcYa.Qu0Nvt6XZ.JtpQUypfAEuvuQ/fvi/N6', '系统管理员', 'admin', 1, '/avatars/admin.jpg', 0, NULL, NOW(3), NOW(3)),
(2011317294235017218, 'dept_admin_cs', '{bcrypt}$2a$10$SG8SLCUzw8nAS/sD.xcYa.Qu0Nvt6XZ.JtpQUypfAEuvuQ/fvi/N6', '计算机学院管理员', 'admin', 1, '/avatars/dept_admin_cs.jpg', 0, NULL, NOW(3), NOW(3));

-- 教师用户
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at) VALUES
(2011317884608471042, 'teacher', '{bcrypt}$2a$10$61NhQEeLs9Ouz8t567krreFBWcmzoqQjRTwC51YehoKjva7RWJHUK', '教师用户', 'teacher', 1, '/avatars/teacher.jpg', '2026-02-13 20:58:01.145', '127.0.0.1', 0, NULL, '2026-01-14 14:02:06.831', NOW(3)),
(2011317884608471043, 'teacher002', '{bcrypt}$2a$10$61NhQEeLs9Ouz8t567krreFBWcmzoqQjRTwC51YehoKjva7RWJHUK', '李老师', 'teacher', 1, '/avatars/teacher002.jpg', NULL, NULL, 0, NULL, NOW(3), NOW(3)),
(2011317884608471044, 'teacher003', '{bcrypt}$2a$10$61NhQEeLs9Ouz8t567krreFBWcmzoqQjRTwC51YehoKjva7RWJHUK', '王老师', 'teacher', 1, '/avatars/teacher003.jpg', NULL, NULL, 0, NULL, NOW(3), NOW(3));

-- 学生用户
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at) VALUES
(2011317642420969473, 'student', '{bcrypt}$2a$10$l9WAyGvuGCrEbXPJvreFCubrV9afr/Wx.L/9nK7KqPP78yi9AHM5e', '学生用户', 'student', 1, '/avatars/student.jpg', '2026-02-13 21:24:03.392', '127.0.0.1', 0, NULL, '2026-01-14 14:01:09.088', NOW(3)),
(2011317642420969474, 'student002', '{bcrypt}$2a$10$l9WAyGvuGCrEbXPJvreFCubrV9afr/Wx.L/9nK7KqPP78yi9AHM5e', '张同学', 'student', 1, '/avatars/student002.jpg', NULL, NULL, 0, NULL, NOW(3), NOW(3)),
(2011317642420969475, 'student003', '{bcrypt}$2a$10$l9WAyGvuGCrEbXPJvreFCubrV9afr/Wx.L/9nK7KqPP78yi9AHM5e', '李同学', 'student', 1, '/avatars/student003.jpg', NULL, NULL, 0, NULL, NOW(3), NOW(3));

-- ==================== 4. 插入用户角色关联数据 ====================
INSERT INTO sys_user_role (id, user_id, role_code, created_at, updated_at) VALUES
(1, 2011317294235017217, 'ROLE_SYSTEM_ADMIN', NOW(3), NOW(3)),
(2, 2011317294235017218, 'ROLE_DEPARTMENT_ADMIN', NOW(3), NOW(3)),
(3, 2011317884608471042, 'ROLE_TEACHER', NOW(3), NOW(3)),
(4, 2011317884608471043, 'ROLE_TEACHER', NOW(3), NOW(3)),
(5, 2011317884608471044, 'ROLE_TEACHER', NOW(3), NOW(3)),
(6, 2011317642420969473, 'ROLE_STUDENT', NOW(3), NOW(3)),
(7, 2011317642420969474, 'ROLE_STUDENT', NOW(3), NOW(3)),
(8, 2011317642420969475, 'ROLE_STUDENT', NOW(3), NOW(3));

-- ==================== 5. 插入业务数据 ====================
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

-- ==================== 6. 插入课题数据 ====================
INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, created_at, updated_at) VALUES
(1, '基于Spring Boot的高校毕业设计管理系统设计与实现', '设计并实现一个完整的高校毕业设计管理系统，包括选题、文档管理、成绩评定等功能模块。', 1, 1, '教学实践', '应用开发', '工程设计', 3, 4, 2, 1, 3, NOW(3), NOW(3)),
(2, '人工智能在图像识别中的应用研究', '研究深度学习算法在图像识别领域的应用，实现一个图像分类系统。', 1, 1, '科研项目', '理论研究', '科学研究', 4, 5, 1, 1, 3, NOW(3), NOW(3)),
(3, '物联网智能家居控制系统设计', '基于物联网技术设计智能家居控制系统，实现设备远程控制和自动化管理。', 2, 1, '企业合作', '应用开发', '工程设计', 3, 4, 1, 0, 1, NOW(3), NOW(3)),
(4, '基于区块链的数据安全存储系统', '研究区块链技术在数据安全存储方面的应用，设计分布式存储解决方案。', 3, 2, '前沿技术', '理论研究', '科学研究', 5, 5, 1, 1, 3, NOW(3), NOW(3)),
(5, '移动应用开发中的用户体验优化研究', '研究移动应用开发中的用户体验设计原则和优化方法。', 2, 1, '教学实践', '应用研究', '工程设计', 2, 3, 2, 0, 1, NOW(3), NOW(3)),
(6, '大数据分析平台在电商推荐系统中的应用', '构建基于大数据分析的电商商品推荐平台，研究用户行为分析和个性化推荐算法。', 1, 1, '企业合作', '应用开发', '工程设计', 4, 5, 1, 0, 1, NOW(3), NOW(3)),
(7, '机器学习在金融风控中的应用研究', '研究机器学习算法在金融风险控制中的应用，构建智能风控模型。', 3, 2, '科研项目', '理论研究', '科学研究', 5, 5, 1, 0, 1, NOW(3), NOW(3));

-- ==================== 7. 插入选题数据 ====================
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at) VALUES
(1, 1, 1, '基于Spring Boot的高校毕业设计管理系统设计与实现', 3, 2011317884608471042, NOW(3), '选题符合要求，同意通过。', NOW(3), NOW(3), NOW(3)),
(2, 2, 2, '人工智能在图像识别中的应用研究', 3, 2011317884608471042, NOW(3), '选题具有研究价值，同意通过。', NOW(3), NOW(3), NOW(3)),
(3, 3, 4, '基于区块链的数据安全存储系统', 3, 2011317884608471044, NOW(3), '选题新颖，同意通过。', NOW(3), NOW(3), NOW(3));

-- ==================== 8. 插入文档数据 ====================
INSERT INTO biz_document (id, user_id, topic_id, file_type, original_filename, stored_path, file_size, review_status, reviewed_at, reviewer_id, feedback, uploaded_at, created_at, updated_at) VALUES
(1, 2011317642420969473, 1, 0, '开题报告_学生.docx', '/uploads/documents/2024/01/开题报告_学生.docx', 1024000, 1, NOW(3), 2011317884608471042, '开题报告内容完整，格式规范。', NOW(3), NOW(3), NOW(3)),
(2, 2011317642420969473, 1, 1, '中期报告_学生.pdf', '/uploads/documents/2024/01/中期报告_学生.pdf', 2048000, 1, NOW(3), 2011317884608471042, '中期进展良好，按时完成各项任务。', NOW(3), NOW(3), NOW(3)),
(3, 2011317642420969473, 1, 2, '毕业论文_学生.pdf', '/uploads/documents/2024/01/毕业论文_学生.pdf', 3072000, 1, NOW(3), 2011317884608471042, '论文结构完整，论证充分，达到毕业要求。', NOW(3), NOW(3), NOW(3)),
(4, 2011317642420969474, 2, 0, '开题报告_张同学.docx', '/uploads/documents/2024/01/开题报告_张同学.docx', 980000, 1, NOW(3), 2011317884608471042, '开题思路清晰，技术路线可行。', NOW(3), NOW(3), NOW(3)),
(5, 2011317642420969475, 4, 0, '开题报告_李同学.docx', '/uploads/documents/2024/01/开题报告_李同学.docx', 1100000, 1, NOW(3), 2011317884608471044, '选题前沿，研究方案合理。', NOW(3), NOW(3), NOW(3));

-- ==================== 9. 插入成绩数据 ====================
INSERT INTO biz_grade (id, student_id, topic_id, score, grader_id, comment, graded_at, created_at, updated_at) VALUES
(1, 1, 1, 85.50, 2011317884608471042, '系统功能完整，界面友好，文档规范。答辩表现优秀。', NOW(3), NOW(3), NOW(3)),
(2, 2, 2, 92.00, 2011317884608471042, '研究成果具有创新性，理论分析深入，实验验证充分。', NOW(3), NOW(3), NOW(3)),
(3, 3, 4, 78.25, 2011317884608471044, '基本功能实现完整，但技术深度有待加强。', NOW(3), NOW(3), NOW(3));

-- ==================== 10. 插入系统日志数据 ====================
INSERT INTO sys_log (id, user_id, username, user_type, module, operation, business_id, status, ip_address, duration_ms, error_message, created_at) VALUES
(1, 2011317294235017217, 'admin', 'admin', 'user', '用户登录', NULL, 1, '127.0.0.1', 150, NULL, NOW(3)),
(2, 2011317884608471042, 'teacher', 'teacher', 'topic', '创建课题', 1, 1, '127.0.0.1', 200, NULL, NOW(3)),
(3, 2011317642420969473, 'student', 'student', 'selection', '提交选题申请', 1, 1, '127.0.0.1', 180, NULL, NOW(3)),
(4, 2011317884608471042, 'teacher', 'teacher', 'selection', '审核选题申请', 1, 1, '127.0.0.1', 120, NULL, NOW(3)),
(5, 2011317642420969473, 'student', 'student', 'document', '上传开题报告', 1, 1, '127.0.0.1', 300, NULL, NOW(3)),
(6, 2011317884608471042, 'teacher', 'teacher', 'document', '审核开题报告', 1, 1, '127.0.0.1', 150, NULL, NOW(3)),
(7, 2011317642420969473, 'student', 'student', 'grade', '查看成绩', NULL, 1, '127.0.0.1', 80, NULL, NOW(3)),
(8, 2011317294235017218, 'dept_admin_cs', 'admin', 'department', '查看院系数据', NULL, 1, '127.0.0.1', 100, NULL, NOW(3)),
(9, 2011317884608471043, 'teacher002', 'teacher', 'topic', '创建课题', 6, 1, '127.0.0.1', 220, NULL, NOW(3)),
(10, 2011317884608471044, 'teacher003', 'teacher', 'topic', '创建课题', 7, 1, '127.0.0.1', 180, NULL, NOW(3));

-- ==================== 初始化完成提示 ====================
SELECT '==================================================' AS separator;
SELECT '数据库清空并重新初始化完成！' AS message;
SELECT '所有表数据已清空并重新插入' AS info;
SELECT '==================================================' AS separator;
SELECT '可以使用以下账号进行测试:' AS test_accounts;
SELECT '' AS empty_line;
SELECT '系统管理员: admin/admin123' AS admin_account;
SELECT '院系管理员: dept_admin_cs/admin123' AS dept_admin_account;
SELECT '教师账号: teacher/teacher123' AS teacher_account;
SELECT '学生账号: student/student123' AS student_account;
SELECT '' AS empty_line;
SELECT '==================================================' AS separator;
SELECT '初始化数据概览:' AS overview;
SELECT CONCAT('院系数量: ', (SELECT COUNT(*) FROM sys_department)) AS department_count;
SELECT CONCAT('用户数量: ', (SELECT COUNT(*) FROM sys_user)) AS user_count;
SELECT CONCAT('管理员数量: ', (SELECT COUNT(*) FROM biz_admin)) AS admin_count;
SELECT CONCAT('教师数量: ', (SELECT COUNT(*) FROM biz_teacher)) AS teacher_count;
SELECT CONCAT('学生数量: ', (SELECT COUNT(*) FROM biz_student)) AS student_count;
SELECT CONCAT('课题数量: ', (SELECT COUNT(*) FROM biz_topic)) AS topic_count;
SELECT CONCAT('选题数量: ', (SELECT COUNT(*) FROM biz_selection)) AS selection_count;
SELECT CONCAT('文档数量: ', (SELECT COUNT(*) FROM biz_document)) AS document_count;
SELECT CONCAT('成绩数量: ', (SELECT COUNT(*) FROM biz_grade)) AS grade_count;
SELECT CONCAT('日志数量: ', (SELECT COUNT(*) FROM sys_log)) AS log_count;
SELECT '' AS empty_line;
SELECT '注意：所有密码均为 bcrypt 加密格式' AS password_note;
SELECT '==================================================' AS separator;