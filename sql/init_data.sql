-- graduation_system_init_data.sql
-- 高校毕业设计论文管理系统 - 初始化数据脚本
-- 基于雪花 ID 生成算法，不使用数据库自增
-- 适用于开发、测试环境快速初始化
-- 
-- ⚠️ 使用说明:
-- 1. 首次运行前必须先执行建表脚本 (sys.sql)
-- 2. 重复运行时会自动清空所有表数据
-- 3. 清空策略：关闭外键 → TRUNCATE 所有表 → 开启外键 → 插入新数据

USE graduation_system;

-- ============================
-- 零、清空现有数据（简单高效）
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
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_department;

SET FOREIGN_KEY_CHECKS = 1;

-- ✅ TRUNCATE 方式优势:
-- - 保留表结构，清空数据
-- - 执行速度快（不记录单条 DELETE 日志）
-- - 彻底清空数据（包括自增 ID 归零）
-- - 无主键冲突风险（完全清空后重新插入）
-- - 无需处理备份表（简单直接）

-- ============================
-- 一、院系数据初始化
-- ============================

-- 计算机科学与技术学院
INSERT INTO sys_department (id, code, name, created_at, updated_at, is_deleted) VALUES
(1700000000000000001, 'CS001', '计算机科学与技术学院', NOW(3), NOW(3), 0),
-- 软件学院
(1700000000000000002, 'SE002', '软件学院', NOW(3), NOW(3), 0),
-- 信息工程学院
(1700000000000000003, 'IE003', '信息工程学院', NOW(3), NOW(3), 0),
-- 人工智能学院
(1700000000000000004, 'AI004', '人工智能学院', NOW(3), NOW(3), 0);

-- ============================
-- 二、系统用户数据初始化
-- ============================

-- 密码设计:
-- 管理员账户：admin123 -> BCrypt hash: $2a$10$7Jr9oeN8qKzHb0uRkVv5L.3hMJQqvXwYpGjFhCnDxPzT4WfE8yNKy
-- 教师账户：teacher123 -> BCrypt hash: $2a$10$9XqH8YvN7KpLmZrTcUwOe.x5DjGfBnChQyWsE3PtA6RiM9FjLkS2
-- 学生账户：student123 -> BCrypt hash: $2a$10$2HsK9LpMnQrTvXwYzBeNc.u6EjHgCnDiRzYtF4QuB7SjN0GkMpT3

-- 系统管理员 (密码：admin123)
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at, is_deleted) VALUES
(1800000000000000001, 'sys_admin', '$2a$10$7Jr9oeN8qKzHb0uRkVv5L.3hMJQqvXwYpGjFhCnDxPzT4WfE8yNKy', '系统管理员', 'system_admin', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0);

-- 院系管理员 (计算机学院) (密码：admin123)
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at, is_deleted) VALUES
(1800000000000000002, 'dept_admin_cs', '$2a$10$7Jr9oeN8qKzHb0uRkVv5L.3hMJQqvXwYpGjFhCnDxPzT4WfE8yNKy', '张主任', 'department_admin', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0);

-- 院系管理员 (软件学院) (密码：admin123)
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at, is_deleted) VALUES
(1800000000000000003, 'dept_admin_se', '$2a$10$7Jr9oeN8qKzHb0uRkVv5L.3hMJQqvXwYpGjFhCnDxPzT4WfE8yNKy', '李主任', 'department_admin', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0);

-- 教师用户 (密码：teacher123)
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at, is_deleted) VALUES
(1800000000000000010, 'teacher001', '$2a$10$9XqH8YvN7KpLmZrTcUwOe.x5DjGfBnChQyWsE3PtA6RiM9FjLkS2', '王教授', 'teacher', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0),
(1800000000000000011, 'teacher002', '$2a$10$9XqH8YvN7KpLmZrTcUwOe.x5DjGfBnChQyWsE3PtA6RiM9FjLkS2', '刘副教授', 'teacher', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0),
(1800000000000000012, 'teacher003', '$2a$10$9XqH8YvN7KpLmZrTcUwOe.x5DjGfBnChQyWsE3PtA6RiM9FjLkS2', '陈讲师', 'teacher', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0);

-- 学生用户 (密码：student123)
INSERT INTO sys_user (id, username, password, real_name, user_type, status, avatar, last_login_at, last_login_ip, login_fail_count, locked_until, created_at, updated_at, is_deleted) VALUES
(1800000000000000020, '2022001', '$2a$10$2HsK9LpMnQrTvXwYzBeNc.u6EjHgCnDiRzYtF4QuB7SjN0GkMpT3', '张三', 'student', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0),
(1800000000000000021, '2022002', '$2a$10$2HsK9LpMnQrTvXwYzBeNc.u6EjHgCnDiRzYtF4QuB7SjN0GkMpT3', '李四', 'student', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0),
(1800000000000000022, '2022003', '$2a$10$2HsK9LpMnQrTvXwYzBeNc.u6EjHgCnDiRzYtF4QuB7SjN0GkMpT3', '王五', 'student', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0),
(1800000000000000023, '2022004', '$2a$10$2HsK9LpMnQrTvXwYzBeNc.u6EjHgCnDiRzYtF4QuB7SjN0GkMpT3', '赵六', 'student', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0),
(1800000000000000024, '2022005', '$2a$10$2HsK9LpMnQrTvXwYzBeNc.u6EjHgCnDiRzYtF4QuB7SjN0GkMpT3', '钱七', 'student', 1, NULL, NULL, NULL, 0, NULL, NOW(3), NOW(3), 0);

-- ============================
-- 三、业务详细信息初始化
-- ============================

-- 管理员业务信息
INSERT INTO biz_admin (id, user_id, admin_id, department_id, role_level, phone, email, created_at, updated_at, is_deleted) VALUES
(1900000000000000001, 1800000000000000001, 'ADMIN001', NULL, 0, '13800138000', 'admin@university.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000002, 1800000000000000002, 'ADMIN002', 1700000000000000001, 1, '13800138001', 'cs_admin@university.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000003, 1800000000000000003, 'ADMIN003', 1700000000000000002, 1, '13800138002', 'se_admin@university.edu.cn', NOW(3), NOW(3), 0);

-- 教师业务信息
INSERT INTO biz_teacher (id, user_id, teacher_id, department_id, gender, title, phone, email, created_at, updated_at, is_deleted) VALUES
(1900000000000000010, 1800000000000000010, 'T001', 1700000000000000001, 1, '教授', '13800138010', 'wang@university.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000011, 1800000000000000011, 'T002', 1700000000000000001, 1, '副教授', '13800138011', 'liu@university.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000012, 1800000000000000012, 'T003', 1700000000000000002, 0, '讲师', '13800138012', 'chen@university.edu.cn', NOW(3), NOW(3), 0);

-- 学生业务信息
INSERT INTO biz_student (id, user_id, student_id, department_id, gender, major, class_name, phone, email, created_at, updated_at, is_deleted) VALUES
(1900000000000000020, 1800000000000000020, 'S2022001', 1700000000000000001, 1, '计算机科学与技术', '计科 2201', '13800138020', 'zhangsan@student.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000021, 1800000000000000021, 'S2022002', 1700000000000000001, 0, '软件工程', '软件 2201', '13800138021', 'lisi@student.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000022, 1800000000000000022, 'S2022003', 1700000000000000002, 1, '软件工程', '软件 2201', '13800138022', 'wangwu@student.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000023, 1800000000000000023, 'S2022004', 1700000000000000003, 0, '电子信息工程', '电信 2201', '13800138023', 'zhaoliu@student.edu.cn', NOW(3), NOW(3), 0),
(1900000000000000024, 1800000000000000024, 'S2022005', 1700000000000000004, 1, '人工智能', '智能 2201', '13800138024', 'qianqi@student.edu.cn', NOW(3), NOW(3), 0);

-- ============================
-- 四、毕业设计题目初始化
-- ============================

-- 题目 1: 开放状态 (OPEN=1, 王教授发布)
INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, reviewer_id, reviewed_at, created_at, updated_at, is_deleted) VALUES
(2000000000000000001, 
'基于深度学习的图像识别系统设计与实现', 
'本课题要求设计并实现一个基于深度学习的图像识别系统。主要内容包括：1. 研究 CNN 等深度学习算法；2. 使用 TensorFlow 或 PyTorch 框架；3. 构建图像分类模型；4. 开发 Web 应用展示结果。适合对 AI 感兴趣的同学。',
1900000000000000010, 1700000000000000001, '科研项目', '理论研究', '工程设计', 4, 4, 3, 0, 1, NULL, NULL, NOW(3), NOW(3), 0);

-- 题目 2: 审核中状态 (REVIEWING=2, 刘副教授发布)
-- reviewer_id: 1800000000000000002 (院系管理员 dept_admin_cs 的用户 ID)
INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, reviewer_id, reviewed_at, created_at, updated_at, is_deleted) VALUES
(2000000000000000002, 
'企业级微服务架构设计与实践', 
'研究微服务架构的核心技术，包括服务注册发现、配置管理、负载均衡、熔断机制等。使用 Spring Cloud Alibaba 技术栈，实现一个完整的电商平台。要求熟悉 Java 编程和分布式系统原理。',
1900000000000000011, 1700000000000000001, '企业合作', '应用开发', '工程设计', 5, 5, 2, 1, 2, 1800000000000000002, NULL, NOW(3), NOW(3), 0);

-- 题目 3: 已选状态 (SELECTED=3, 陈讲师发布)
-- reviewer_id: 1800000000000000003 (院系管理员 dept_admin_se 的用户 ID)
INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, reviewer_id, reviewed_at, created_at, updated_at, is_deleted) VALUES
(2000000000000000003, 
'移动应用跨平台开发框架对比研究', 
'对比分析 React Native、Flutter、Uni-app 等主流跨平台开发框架的性能、生态和适用场景。实现同一个应用的多个版本，进行性能测试和用户体验评估。',
1900000000000000012, 1700000000000000002, '教学实践', '理论研究', '科学研究', 3, 3, 5, 5, 3, 1800000000000000003, NOW(3), NOW(3), NOW(3), 0);

-- 题目 4: 关闭状态 (CLOSED=4, 王教授发布)
-- reviewer_id: 1800000000000000001 (系统管理员 sys_admin 的用户 ID)
INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, reviewer_id, reviewed_at, created_at, updated_at, is_deleted) VALUES
(2000000000000000004, 
'区块链技术在供应链管理中的应用', 
'研究区块链技术的基本原理，设计基于区块链的供应链管理系统。实现产品溯源、信息不可篡改等功能。需要了解智能合约和分布式账本技术。',
1900000000000000010, 1700000000000000001, '社会实践', '应用开发', '科学研究', 4, 4, 2, 2, 4, 1800000000000000001, NOW(3), NOW(3), NOW(3), 0);

-- 题目 5: 开放状态 (OPEN=1, 刘副教授发布)
INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, reviewer_id, reviewed_at, created_at, updated_at, is_deleted) VALUES
(2000000000000000005, 
'大数据驱动的用户行为分析平台', 
'基于 Hadoop/Spark 构建大数据分析平台，对用户行为数据进行采集、存储、分析和可视化。使用机器学习算法进行用户画像和推荐。处理 TB 级数据。',
1900000000000000011, 1700000000000000001, '科研项目', '应用开发', '工程设计', 5, 5, 3, 0, 1, NULL, NULL, NOW(3), NOW(3), 0);

-- ============================
-- 五、选题记录初始化
-- ============================

-- 张三选择了题目 2(待审核 PENDING_REVIEW=0)
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at, is_deleted) VALUES
(2100000000000000001, 1900000000000000020, 2000000000000000002, '企业级微服务架构设计与实践', 0, NULL, NULL, NULL, NULL, NOW(3), NOW(3), 0);

-- 李四选择了题目 2(审核通过 APPROVED=1)
-- reviewer_id: 1800000000000000002 (院系管理员 dept_admin_cs 的用户 ID)
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at, is_deleted) VALUES
(2100000000000000002, 1900000000000000021, 2000000000000000002, '企业级微服务架构设计与实践', 1, 1800000000000000002, NOW(3), '同意该生的选题申请', NOW(3), NOW(3), NOW(3), 0);

-- 王五确认了题目 3(已确认 CONFIRMED=3)
-- reviewer_id: 1800000000000000003 (院系管理员 dept_admin_se 的用户 ID)
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at, is_deleted) VALUES
(2100000000000000003, 1900000000000000022, 2000000000000000003, '移动应用跨平台开发框架对比研究', 3, 1800000000000000003, NOW(3), '题目很好，认真研究', NOW(3), NOW(3), NOW(3), 0);

-- 赵六选择了题目 3(审核通过 APPROVED=1，未确认)
-- reviewer_id: 1800000000000000003 (院系管理员 dept_admin_se 的用户 ID)
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at, is_deleted) VALUES
(2100000000000000004, 1900000000000000023, 2000000000000000003, '移动应用跨平台开发框架对比研究', 1, 1800000000000000003, NOW(3), '可以开始研究', NULL, NOW(3), NOW(3), 0);

-- 钱七选择了题目 3(审核通过 APPROVED=1，未确认)
-- reviewer_id: 1800000000000000003 (院系管理员 dept_admin_se 的用户 ID)
INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at, is_deleted) VALUES
(2100000000000000005, 1900000000000000024, 2000000000000000003, '移动应用跨平台开发框架对比研究', 1, 1800000000000000003, NOW(3), '同意', NULL, NOW(3), NOW(3), 0);

-- ============================
-- 六、通知公告初始化
-- ============================

-- 系统通知 - 已发布置顶
INSERT INTO biz_notice (id, title, content, type, priority, publisher_id, published_at, start_time, end_time, status, is_sticky, read_count, target_scope, attachment_url, created_at, updated_at, is_deleted) VALUES
(2200000000000000001, 
'关于 2026 届毕业设计选题工作的通知', 
'各学院、各位师生：\n\n2026 届本科毕业设计选题工作即将开始，现将有关事项通知如下：\n\n一、选题时间：2026 年 3 月 1 日 -3 月 15 日\n二、选题对象：2022 级全体本科生\n三、选题方式：登录系统进行网上选题\n四、注意事项：每位学生限选一个题目，先选先得\n\n请各位同学按时登录系统完成选题，逾期将影响毕业进度。\n\n教务处\n2026 年 2 月 25 日',
1, 3, 1800000000000000001, NOW(3), NOW(3), NULL, 1, 1, 156, 0, NULL, NOW(3), NOW(3), 0);

-- 公告 - 已发布
INSERT INTO biz_notice (id, title, content, type, priority, publisher_id, published_at, start_time, end_time, status, is_sticky, read_count, target_scope, attachment_url, created_at, updated_at, is_deleted) VALUES
(2200000000000000002, 
'计算机学院毕业设计答辩安排', 
'各位同学：\n\n计算机学院 2026 届毕业设计答辩安排如下：\n\n一、答辩时间：2026 年 5 月 20 日 -5 月 25 日\n二、答辩地点：计算机大楼 301-310 教室\n三、分组安排：详见学院网站通知\n四、材料提交：5 月 15 日前提交所有材料\n\n请同学们认真准备，按时参加答辩。\n\n计算机学院\n2026 年 3 月 10 日',
2, 3, 1800000000000000002, NOW(3), NOW(3), NULL, 1, 0, 89, 1, NULL, NOW(3), NOW(3), 0);

-- 提醒 - 已发布
INSERT INTO biz_notice (id, title, content, type, priority, publisher_id, published_at, start_time, end_time, status, is_sticky, read_count, target_scope, attachment_url, created_at, updated_at, is_deleted) VALUES
(2200000000000000003, 
'中期检查报告提交提醒', 
'各位同学：\n\n毕业设计中期检查报告提交截止日期为 2026 年 4 月 10 日，请尚未提交的同学尽快登录系统提交中期报告。\n\n提交路径：个人中心 -> 文档管理 -> 上传中期报告\n\n如有疑问，请联系指导教师。\n\n教务处\n2026 年 4 月 1 日',
3, 2, 1800000000000000001, NOW(3), NOW(3), NULL, 1, 0, 234, 1, NULL, NOW(3), NOW(3), 0);

-- 草稿通知
INSERT INTO biz_notice (id, title, content, type, priority, publisher_id, published_at, start_time, end_time, status, is_sticky, read_count, target_scope, attachment_url, created_at, updated_at, is_deleted) VALUES
(2200000000000000004, 
'关于调整毕业设计进度的通知（草稿）', 
'因疫情原因，经学校研究决定，对 2026 届毕业设计进度进行适当调整...\n\n（此通知暂未发布）',
1, 2, 1800000000000000001, NULL, NULL, NULL, 0, 0, 0, 0, NULL, NOW(3), NOW(3), 0);

-- ============================
-- 七、成绩数据初始化
-- ============================

-- 王五的题目 3 成绩记录
-- grader_id: 1800000000000000012 (陈讲师的用户 ID)
-- student_id: 1900000000000000022 (王五的学生业务 ID)
-- topic_id: 2000000000000000003 (题目 3 的业务 ID)
INSERT INTO biz_grade (id, student_id, topic_id, grade_type, score, grader_id, comment, graded_at, created_at, updated_at, is_deleted) VALUES
(2300000000000000001, 1900000000000000022, 2000000000000000003, 1, 88.5, 1800000000000000012, '该生在设计过程中表现优秀，系统功能完整，代码规范，论文写作清晰。', NOW(3), NOW(3), NOW(3), 0);

-- ============================
-- 八、系统日志初始化 (示例)
-- ============================

INSERT INTO sys_log (id, user_id, username, user_type, module, operation, business_id, status, ip_address, duration_ms, error_message, created_at) VALUES
(2400000000000000001, 1800000000000000001, 'sys_admin', 'system_admin', 'admin', '系统初始化', NULL, 1, '127.0.0.1', 150, NULL, NOW(3)),
(2400000000000000002, 1800000000000000002, 'dept_admin_cs', 'department_admin', 'user', '审核教师资质', 1900000000000000010, 1, '192.168.1.100', 80, NULL, NOW(3)),
(2400000000000000003, 1800000000000000020, '2022001', 'student', 'topic', '选择毕业设计题目', 2000000000000000002, 1, '192.168.1.101', 120, NULL, NOW(3));

-- ============================
-- 数据初始化完成
-- ============================

-- 数据统计
SELECT '===== 数据初始化完成 =====' AS message;
SELECT CONCAT('院系数量：', COUNT(*)) AS result FROM sys_department;
SELECT CONCAT('用户总数：', COUNT(*)) AS result FROM sys_user;
SELECT CONCAT('其中系统管理员：', COUNT(*)) AS result FROM sys_user WHERE user_type = 'system_admin';
SELECT CONCAT('其中院系管理员：', COUNT(*)) AS result FROM sys_user WHERE user_type = 'department_admin';
SELECT CONCAT('其中教师：', COUNT(*)) AS result FROM sys_user WHERE user_type = 'teacher';
SELECT CONCAT('其中学生：', COUNT(*)) AS result FROM sys_user WHERE user_type = 'student';
SELECT CONCAT('题目总数：', COUNT(*)) AS result FROM biz_topic;
SELECT CONCAT('选题记录数：', COUNT(*)) AS result FROM biz_selection;
SELECT CONCAT('通知总数：', COUNT(*)) AS result FROM biz_notice;
