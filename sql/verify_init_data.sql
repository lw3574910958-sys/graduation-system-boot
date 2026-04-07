-- 数据完整性验证脚本
-- 用于验证init_data.sql中所有外键关联的正确性

USE graduation_system;

-- ============================
-- 1. 验证院系数据
-- ============================
SELECT '=== 院系数据统计 ===' AS info;
SELECT COUNT(*) AS department_count FROM sys_department WHERE is_deleted = 0;
SELECT * FROM sys_department ORDER BY id;

-- ============================
-- 2. 验证用户数据
-- ============================
SELECT '=== 用户数据统计 ===' AS info;
SELECT user_type, COUNT(*) AS count 
FROM sys_user 
WHERE is_deleted = 0 
GROUP BY user_type 
ORDER BY user_type;

-- 验证系统管理员ID范围: 1800000000000000001~010
SELECT '系统管理员ID范围验证' AS check_item,
       MIN(id) AS min_id, MAX(id) AS max_id, COUNT(*) AS count
FROM sys_user 
WHERE user_type = 'system_admin' AND is_deleted = 0;

-- 验证院系管理员ID范围: 1800000000000000011~060
SELECT '院系管理员ID范围验证' AS check_item,
       MIN(id) AS min_id, MAX(id) AS max_id, COUNT(*) AS count
FROM sys_user 
WHERE user_type = 'department_admin' AND is_deleted = 0;

-- 验证教师ID范围: 1800000000000000061~200
SELECT '教师ID范围验证' AS check_item,
       MIN(id) AS min_id, MAX(id) AS max_id, COUNT(*) AS count
FROM sys_user 
WHERE user_type = 'teacher' AND is_deleted = 0;

-- 验证学生ID范围: 1800000000000000201~500
SELECT '学生ID范围验证' AS check_item,
       MIN(id) AS min_id, MAX(id) AS max_id, COUNT(*) AS count
FROM sys_user 
WHERE user_type = 'student' AND is_deleted = 0;

-- ============================
-- 3. 验证业务详细信息外键关联
-- ============================

-- 3.1 验证管理员业务信息
SELECT '=== 管理员业务信息验证 ===' AS info;
SELECT COUNT(*) AS admin_biz_count FROM biz_admin WHERE is_deleted = 0;

-- 验证user_id是否存在于sys_user
SELECT '无效user_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_admin ba
LEFT JOIN sys_user u ON ba.user_id = u.id
WHERE ba.is_deleted = 0 AND u.id IS NULL;

-- 验证department_id是否存在于sys_department
SELECT '无效department_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_admin ba
LEFT JOIN sys_department d ON ba.department_id = d.id
WHERE ba.is_deleted = 0 AND d.id IS NULL;

-- 3.2 验证教师业务信息
SELECT '=== 教师业务信息验证 ===' AS info;
SELECT COUNT(*) AS teacher_biz_count FROM biz_teacher WHERE is_deleted = 0;

SELECT '无效user_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_teacher bt
LEFT JOIN sys_user u ON bt.user_id = u.id
WHERE bt.is_deleted = 0 AND u.id IS NULL;

SELECT '无效department_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_teacher bt
LEFT JOIN sys_department d ON bt.department_id = d.id
WHERE bt.is_deleted = 0 AND d.id IS NULL;

-- 3.3 验证学生业务信息
SELECT '=== 学生业务信息验证 ===' AS info;
SELECT COUNT(*) AS student_biz_count FROM biz_student WHERE is_deleted = 0;

SELECT '无效user_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_student bs
LEFT JOIN sys_user u ON bs.user_id = u.id
WHERE bs.is_deleted = 0 AND u.id IS NULL;

SELECT '无效department_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_student bs
LEFT JOIN sys_department d ON bs.department_id = d.id
WHERE bs.is_deleted = 0 AND d.id IS NULL;

-- ============================
-- 4. 验证题目数据外键关联
-- ============================
SELECT '=== 题目数据统计 ===' AS info;
SELECT status, COUNT(*) AS count
FROM biz_topic
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;

SELECT '无效teacher_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_topic t
LEFT JOIN biz_teacher bt ON t.teacher_id = bt.id
WHERE t.is_deleted = 0 AND bt.id IS NULL;

SELECT '无效department_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_topic t
LEFT JOIN sys_department d ON t.department_id = d.id
WHERE t.is_deleted = 0 AND d.id IS NULL;

-- 验证selected_count <= max_selections约束
SELECT '违反selected_count约束的题目数' AS check_item, COUNT(*) AS invalid_count
FROM biz_topic
WHERE selected_count > max_selections AND is_deleted = 0;

-- ============================
-- 5. 验证选题数据外键关联
-- ============================
SELECT '=== 选题数据统计 ===' AS info;
SELECT status, COUNT(*) AS count
FROM biz_selection
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;

SELECT '无效student_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_selection s
LEFT JOIN biz_student bs ON s.student_id = bs.id
WHERE s.is_deleted = 0 AND bs.id IS NULL;

SELECT '无效topic_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_selection s
LEFT JOIN biz_topic t ON s.topic_id = t.id
WHERE s.is_deleted = 0 AND t.id IS NULL;

-- 验证CONFIRMED选题的唯一性约束（每个学生只能有一个CONFIRMED选题）
SELECT '重复CONFIRMED选题的学生数' AS check_item, COUNT(*) AS duplicate_count
FROM (
    SELECT student_id, COUNT(*) AS cnt
    FROM biz_selection
    WHERE status = 3 AND is_deleted = 0
    GROUP BY student_id
    HAVING cnt > 1
) AS dup;

-- ============================
-- 6. 验证文档数据外键关联
-- ============================
SELECT '=== 文档数据统计 ===' AS info;
SELECT file_type, review_status, COUNT(*) AS count
FROM biz_document
WHERE is_deleted = 0
GROUP BY file_type, review_status
ORDER BY file_type, review_status;

SELECT '无效user_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_document d
LEFT JOIN sys_user u ON d.user_id = u.id
WHERE d.is_deleted = 0 AND u.id IS NULL;

SELECT '无效topic_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_document d
LEFT JOIN biz_topic t ON d.topic_id = t.id
WHERE d.is_deleted = 0 AND t.id IS NULL;

-- 验证APPROVED文档的reviewer_id是否存在
SELECT 'APPROVED文档无效reviewer_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_document d
LEFT JOIN biz_teacher bt ON d.reviewer_id = bt.id
WHERE d.is_deleted = 0 AND d.review_status = 1 AND bt.id IS NULL;

-- ============================
-- 7. 验证成绩数据外键关联
-- ============================
SELECT '=== 成绩数据统计 ===' AS info;
SELECT grade_type, COUNT(*) AS count
FROM biz_grade
WHERE is_deleted = 0
GROUP BY grade_type
ORDER BY grade_type;

SELECT '无效student_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_grade g
LEFT JOIN biz_student bs ON g.student_id = bs.id
WHERE g.is_deleted = 0 AND bs.id IS NULL;

SELECT '无效topic_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_grade g
LEFT JOIN biz_topic t ON g.topic_id = t.id
WHERE g.is_deleted = 0 AND t.id IS NULL;

SELECT '无效grader_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_grade g
LEFT JOIN biz_teacher bt ON g.grader_id = bt.id
WHERE g.is_deleted = 0 AND bt.id IS NULL;

-- 验证唯一约束：student_id + topic_id + grade_type
SELECT '重复成绩记录数' AS check_item, COUNT(*) AS duplicate_count
FROM (
    SELECT student_id, topic_id, grade_type, COUNT(*) AS cnt
    FROM biz_grade
    WHERE is_deleted = 0
    GROUP BY student_id, topic_id, grade_type
    HAVING cnt > 1
) AS dup;

-- ============================
-- 8. 验证公告数据外键关联
-- ============================
SELECT '=== 公告数据统计 ===' AS info;
SELECT status, COUNT(*) AS count
FROM biz_notice
WHERE is_deleted = 0
GROUP BY status
ORDER BY status;

SELECT '无效publisher_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_notice n
LEFT JOIN sys_user u ON n.publisher_id = u.id
WHERE n.is_deleted = 0 AND u.id IS NULL;

-- 验证有department_id的公告是否有效
SELECT '无效department_id数量' AS check_item, COUNT(*) AS invalid_count
FROM biz_notice n
LEFT JOIN sys_department d ON n.department_id = d.id
WHERE n.is_deleted = 0 AND n.department_id IS NOT NULL AND d.id IS NULL;

-- ============================
-- 9. 综合统计
-- ============================
SELECT '=== 最终数据统计汇总 ===' AS info;
SELECT 
    (SELECT COUNT(*) FROM sys_department WHERE is_deleted = 0) AS departments,
    (SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0) AS users,
    (SELECT COUNT(*) FROM biz_admin WHERE is_deleted = 0) AS admins,
    (SELECT COUNT(*) FROM biz_teacher WHERE is_deleted = 0) AS teachers,
    (SELECT COUNT(*) FROM biz_student WHERE is_deleted = 0) AS students,
    (SELECT COUNT(*) FROM biz_topic WHERE is_deleted = 0) AS topics,
    (SELECT COUNT(*) FROM biz_selection WHERE is_deleted = 0) AS selections,
    (SELECT COUNT(*) FROM biz_document WHERE is_deleted = 0) AS documents,
    (SELECT COUNT(*) FROM biz_grade WHERE is_deleted = 0) AS grades,
    (SELECT COUNT(*) FROM biz_notice WHERE is_deleted = 0) AS notices;

SELECT '=== 验证完成 ===' AS info;
