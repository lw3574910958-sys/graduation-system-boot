#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
高校毕业设计管理系统 - 大规模测试数据生成脚本
使用方法: python generate_init_data.py
"""

import os
from datetime import datetime, timedelta

# ==================== 配置区 ====================
OUTPUT_FILE = "init_data_generated.sql"
BCRYPT_PASSWORD = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi"

DEPT_COUNT = 10
SYS_ADMIN_COUNT = 10
DEPT_ADMIN_COUNT = 50
TEACHER_COUNT = 140
STUDENT_COUNT = 300
TOPIC_COUNT = 600
SELECTION_COUNT = 200
DOCUMENT_COUNT = 400
GRADE_COUNT = 200
NOTICE_COUNT = 20

def format_dt(dt):
    return dt.strftime('%Y-%m-%d %H:%M:%S.') + f"{dt.microsecond // 1000:03d}"

def main():
    now = datetime.now()
    
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        # 文件头
        f.write("-- graduation_system_init_data.sql\n")
        f.write("-- 高校毕业设计论文管理系统 - 初始化数据脚本（大规模测试数据版）\n")
        f.write("-- 基于雪花 ID 生成算法，不使用数据库自增\n")
        f.write("-- 由Python脚本自动生成，包含大量测试数据覆盖所有业务场景\n\n")
        
        f.write("USE graduation_system;\n\n")
        
        # 清空数据
        f.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")
        for table in ['sys_log', 'biz_grade', 'biz_selection', 'biz_document', 
                      'biz_notice', 'biz_topic', 'biz_student', 'biz_teacher', 
                      'biz_admin', 'sys_user', 'sys_department']:
            f.write(f"TRUNCATE TABLE {table};\n")
        f.write("\nSET FOREIGN_KEY_CHECKS = 1;\n\n")
        
        # 一、院系
        f.write("-- 一、院系数据初始化（10个）\n")
        f.write("INSERT INTO sys_department (id, code, name, created_at, updated_at, is_deleted) VALUES\n")
        depts = [
            (1700000000000000001, 'CS001', '计算机科学与技术学院'),
            (1700000000000000002, 'SE002', '软件学院'),
            (1700000000000000003, 'IE003', '信息工程学院'),
            (1700000000000000004, 'AI004', '人工智能学院'),
            (1700000000000000005, 'CY005', '网络空间安全学院'),
            (1700000000000000006, 'DS006', '数据科学学院'),
            (1700000000000000007, 'IOT007', '物联网工程学院'),
            (1700000000000000008, 'EE008', '电子工程学院'),
            (1700000000000000009, 'ME009', '机械工程学院'),
            (1700000000000000010, 'CE010', '土木工程学院'),
        ]
        values = []
        for did, code, name in depts:
            values.append(f"({did}, '{code}', '{name}', NOW(3), NOW(3), 0)")
        f.write(",\n".join(values) + ";\n\n")
        
        # 二、用户数据
        f.write("-- 二、系统用户数据初始化（500个）\n\n")
        
        # 2.1 系统管理员
        f.write("-- 2.1 系统管理员（10个）\n")
        f.write("INSERT INTO sys_user (id, username, password, real_name, user_type, status, created_at, updated_at, is_deleted) VALUES\n")
        vals = []
        for i in range(10):
            uid = 1800000000000000001 + i
            vals.append(f"({uid}, 'sys_admin_{i+1:03d}', '{BCRYPT_PASSWORD}', '系统管理员{i+1:02d}', 'system_admin', 1, NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 2.2 院系管理员
        f.write("-- 2.2 院系管理员（50个，每个院系5个）\n")
        f.write("INSERT INTO sys_user (id, username, password, real_name, user_type, status, created_at, updated_at, is_deleted) VALUES\n")
        vals = []
        dept_prefixes = ['cs', 'se', 'ie', 'ai', 'cy', 'ds', 'iot', 'ee', 'me', 'ce']
        dept_names = ['张', '李', '王', '赵', '孙', '周', '吴', '郑', '冯', '陈']
        for i in range(50):
            uid = 1800000000000000011 + i
            dept_idx = i // 5
            prefix = dept_prefixes[dept_idx]
            name = dept_names[dept_idx]
            vals.append(f"({uid}, 'dept_admin_{prefix}_{i%5+1:03d}', '{BCRYPT_PASSWORD}', '{name}主任{i%5+1:02d}', 'department_admin', 1, NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 2.3 教师
        f.write("-- 2.3 教师（140个，每个院系14个）\n")
        f.write("INSERT INTO sys_user (id, username, password, real_name, user_type, status, created_at, updated_at, is_deleted) VALUES\n")
        vals = []
        titles = ['教授', '副教授', '讲师', '助教']
        for i in range(140):
            uid = 1800000000000000061 + i
            dept_idx = i // 14
            prefix = dept_prefixes[dept_idx]
            title = titles[i % 4]
            vals.append(f"({uid}, 'teacher_{prefix}_{i%14+1:03d}', '{BCRYPT_PASSWORD}', '{title}{i%14+1:02d}', 'teacher', 1, NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 2.4 学生
        f.write("-- 2.4 学生（300个，每个院系30个）\n")
        f.write("INSERT INTO sys_user (id, username, password, real_name, user_type, status, created_at, updated_at, is_deleted) VALUES\n")
        vals = []
        for i in range(300):
            uid = 1800000000000000201 + i
            sno = 2022001 + i
            vals.append(f"({uid}, '{sno}', '{BCRYPT_PASSWORD}', '学生{i+1:03d}', 'student', 1, NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 三、业务详细信息
        f.write("-- 三、业务详细信息初始化\n\n")
        
        # 3.1 管理员业务
        f.write("-- 3.1 管理员业务信息（50个）\n")
        f.write("INSERT INTO biz_admin (id, user_id, admin_id, department_id, role_level, phone, email, created_at, updated_at, is_deleted) VALUES\n")
        vals = []
        for i in range(50):
            aid = 1900000000000000001 + i
            uid = 1800000000000000011 + i
            dept_idx = i // 5
            dept_id = 1700000000000000001 + dept_idx
            vals.append(f"({aid}, {uid}, 'ADMIN_{i+1:03d}', {dept_id}, 1, '1380000{1000+i:04d}', 'admin{i+1}@university.edu.cn', NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 3.2 教师业务
        f.write("-- 3.2 教师业务信息（140个）\n")
        f.write("INSERT INTO biz_teacher (id, user_id, teacher_id, department_id, gender, title, phone, email, created_at, updated_at, is_deleted) VALUES\n")
        vals = []
        for i in range(140):
            tid = 1900000000000000061 + i
            uid = 1800000000000000061 + i
            dept_idx = i // 14
            dept_id = 1700000000000000001 + dept_idx
            gender = 1 if i % 2 == 0 else 0
            title = titles[i % 4]
            vals.append(f"({tid}, {uid}, 'T_{i+1:03d}', {dept_id}, {gender}, '{title}', '1380000{2000+i:04d}', 'teacher{i+1}@university.edu.cn', NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 3.3 学生业务
        f.write("-- 3.3 学生业务信息（300个）\n")
        f.write("INSERT INTO biz_student (id, user_id, student_id, department_id, gender, major, class_name, phone, email, created_at, updated_at, is_deleted) VALUES\n")
        majors_map = {
            0: ['计算机科学与技术', '软件工程', '网络工程', '信息安全'],
            1: ['软件工程', '数字媒体技术', '数据科学与大数据技术'],
            2: ['电子信息工程', '通信工程', '光电信息科学与工程'],
            3: ['人工智能', '智能科学与技术', '机器人工程'],
            4: ['网络空间安全', '密码科学与技术'],
            5: ['数据科学与大数据技术', '统计学', '应用数学'],
            6: ['物联网工程', '智能电网信息工程'],
            7: ['电子科学与技术', '微电子科学与工程', '集成电路设计'],
            8: ['机械工程', '车辆工程', '工业设计'],
            9: ['土木工程', '建筑环境与能源应用工程', '给排水科学与工程'],
        }
        vals = []
        for i in range(300):
            sid = 1900000000000000201 + i
            uid = 1800000000000000201 + i
            dept_idx = i // 30
            dept_id = 1700000000000000001 + dept_idx
            gender = 1 if i % 2 == 0 else 0
            majors = majors_map[dept_idx]
            major = majors[i % len(majors)]
            cls = f"{major[:2]}22{(i%30)//10+1:02d}"
            vals.append(f"({sid}, {uid}, '2022{i+1:03d}', {dept_id}, {gender}, '{major}', '{cls}', '1390000{3000+i:04d}', 'student{i+1}@university.edu.cn', NOW(3), NOW(3), 0)")
        f.write(",\n".join(vals) + ";\n\n")
        
        # 四、题目数据
        f.write("-- 四、题目数据初始化（600个）\n")
        f.write("-- 状态分布: DRAFT(60), REVIEWING(60), OPEN(300), CLOSED(180)\n")
        f.write("INSERT INTO biz_topic (id, title, description, teacher_id, department_id, source, type, nature, difficulty, workload, max_selections, selected_count, status, last_review_outcome, reviewer_id, reviewed_at, created_at, updated_at, is_deleted) VALUES\n")
        
        sources = ['教学实践', '科研项目', '企业合作', '前沿技术', '社会实践', '其他']
        types = ['理论研究', '应用开发', '其他']
        natures = ['工程设计', '科学研究', '其他']
        
        vals = []
        for i in range(600):
            topic_id = 2000000000000000001 + i
            teacher_biz_id = 1900000000000000061 + (i % 140)
            dept_idx = (i % 140) // 14
            dept_id = 1700000000000000001 + dept_idx
            
            if i < 60:
                status = 0
                review_outcome = "NULL"
                reviewer = "NULL"
                reviewed_at = "NULL"
                selected_count = 0
            elif i < 120:
                status = 1
                review_outcome = "NULL"
                reviewer = "NULL"
                reviewed_at = "NULL"
                selected_count = 0
            elif i < 420:
                status = 2
                review_outcome = 1
                reviewer = str(1800000000000000001 + (i % 10))
                reviewed_date = now - timedelta(days=30-i%30)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                selected_count = 0
            else:
                status = 3
                review_outcome = 1
                reviewer = str(1800000000000000001 + (i % 10))
                reviewed_date = now - timedelta(days=60-i%30)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                selected_count = 2
            
            source = sources[i % len(sources)]
            type_val = types[i % len(types)]
            nature = natures[i % len(natures)]
            difficulty = (i % 5) + 1
            workload = (i % 5) + 1
            max_sel = 2 if status == 3 else (3 if status == 2 else 1)
            create_date = now - timedelta(days=90-i%60)
            
            vals.append(
                f"({topic_id}, '题目{i+1:03d}-{source}{type_val}', '第{i+1}个题目的详细描述', "
                f"{teacher_biz_id}, {dept_id}, '{source}', '{type_val}', '{nature}', {difficulty}, {workload}, "
                f"{max_sel}, {selected_count}, {status}, {review_outcome}, {reviewer}, {reviewed_at}, "
                f"'{format_dt(create_date)}', '{format_dt(create_date)}', 0)"
            )
        f.write(",\n".join(vals) + ";\n\n")
        
        # 五、选题数据
        f.write("-- 五、选题数据初始化（200个）\n")
        f.write("-- 状态分布: PENDING_REVIEW(40), APPROVED(60), REJECTED(40), CONFIRMED(60)\n")
        f.write("INSERT INTO biz_selection (id, student_id, topic_id, topic_title, status, apply_reason, student_ability, expected_goal, reviewer_id, reviewed_at, review_comment, confirmed_at, created_at, updated_at, is_deleted) VALUES\n")
        
        vals = []
        for i in range(200):
            sel_id = 2100000000000000001 + i
            stu_biz_id = 1900000000000000201 + (i % 300)
            topic_id = 2000000000000000001 + (i % 600)
            
            if i < 40:
                status = 0
                reviewer = "NULL"
                reviewed_at = "NULL"
                comment = "NULL"
                confirmed_at = "NULL"
            elif i < 100:
                status = 1
                reviewer = str(1800000000000000061 + (i % 140))
                reviewed_date = now - timedelta(days=20-i%20)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                comment = "'申请理由充分，同意通过'"
                confirmed_at = "NULL"
            elif i < 140:
                status = 2
                reviewer = str(1800000000000000061 + (i % 140))
                reviewed_date = now - timedelta(days=15-i%15)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                comment = "'申请理由不够充分，建议重新规划'"
                confirmed_at = "NULL"
            else:
                status = 3
                reviewer = str(1800000000000000061 + (i % 140))
                reviewed_date = now - timedelta(days=25-i%20)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                comment = "'同意通过'"
                confirmed_date = reviewed_date + timedelta(days=2)
                confirmed_at = f"'{format_dt(confirmed_date)}'"
            
            create_date = now - timedelta(days=30-i%20)
            
            vals.append(
                f"({sel_id}, {stu_biz_id}, {topic_id}, '题目{(i%600)+1:03d}', {status}, "
                f"'希望研究该领域', '具备相关基础知识', '完成系统设计与实现', "
                f"{reviewer}, {reviewed_at}, {comment}, {confirmed_at}, "
                f"'{format_dt(create_date)}', '{format_dt(create_date)}', 0)"
            )
        f.write(",\n".join(vals) + ";\n\n")
        
        # 六、文档数据
        f.write("-- 六、文档数据初始化（400个）\n")
        f.write("-- 类型分布: PROPOSAL(150), MIDTERM(150), THESIS(100)\n")
        f.write("INSERT INTO biz_document (id, user_id, topic_id, file_type, original_filename, stored_path, file_size, review_status, reviewed_at, reviewer_id, feedback, uploaded_at, created_at, updated_at, is_deleted) VALUES\n")
        
        vals = []
        for i in range(400):
            doc_id = 2200000000000000001 + i
            stu_uid = 1800000000000000201 + (i % 300)
            topic_id = 2000000000000000001 + (i % 600)
            
            if i < 150:
                file_type = 0
                filename = f"学生{i%300+1:03d}_开题报告.pdf"
            elif i < 300:
                file_type = 1
                filename = f"学生{i%300+1:03d}_中期报告.pdf"
            else:
                file_type = 2
                filename = f"学生{i%300+1:03d}_毕业论文.docx"
            
            if i % 4 == 0:
                review_status = 0
                reviewer = "NULL"
                reviewed_at = "NULL"
                feedback = "NULL"
            elif i % 4 == 3:
                review_status = 2
                reviewer = str(1800000000000000061 + (i % 140))
                reviewed_date = now - timedelta(days=10-i%10)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                feedback = "'内容需要完善，请修改后重新提交'"
            else:
                review_status = 1
                reviewer = str(1800000000000000061 + (i % 140))
                reviewed_date = now - timedelta(days=15-i%10)
                reviewed_at = f"'{format_dt(reviewed_date)}'"
                feedback = "'审核通过，符合要求'"
            
            file_size = 500000 + (i % 500000)
            upload_date = now - timedelta(days=20-i%15)
            ext = 'pdf' if file_type < 2 else 'docx'
            stored_path = f"documents/2026/04/doc_{i+1:04d}.{ext}"
            
            vals.append(
                f"({doc_id}, {stu_uid}, {topic_id}, {file_type}, '{filename}', '{stored_path}', "
                f"{file_size}, {review_status}, {reviewed_at}, {reviewer}, {feedback}, "
                f"'{format_dt(upload_date)}', '{format_dt(upload_date)}', '{format_dt(upload_date)}', 0)"
            )
        f.write(",\n".join(vals) + ";\n\n")
        
        # 七、成绩数据
        f.write("-- 七、成绩数据初始化（200个）\n")
        f.write("-- 类型分布: PROPOSAL(60), MIDTERM(60), THESIS(40), COMPOSITE(40)\n")
        f.write("INSERT INTO biz_grade (id, student_id, topic_id, grade_type, score, grader_id, comment, graded_at, created_at, updated_at, is_deleted) VALUES\n")
        
        vals = []
        for i in range(200):
            grade_id = 2300000000000000001 + i
            stu_biz_id = 1900000000000000201 + (i % 300)
            topic_id = 2000000000000000001 + (i % 600)
            
            if i < 60:
                grade_type = 0
            elif i < 120:
                grade_type = 1
            elif i < 160:
                grade_type = 2
            else:
                grade_type = 3
            
            score = round(60 + (i % 40), 2)
            grader_id = 1800000000000000061 + (i % 140)
            graded_date = now - timedelta(days=10-i%10)
            
            vals.append(
                f"({grade_id}, {stu_biz_id}, {topic_id}, {grade_type}, {score}, {grader_id}, "
                f"'成绩评定：{score}分', '{format_dt(graded_date)}', '{format_dt(graded_date)}', "
                f"'{format_dt(graded_date)}', 0)"
            )
        f.write(",\n".join(vals) + ";\n\n")
        
        # 八、通知公告
        f.write("-- 八、通知公告数据初始化（20个）\n")
        f.write("-- 状态分布: DRAFT(2), PUBLISHED(15), WITHDRAWN(2), SCHEDULED(1)\n")
        f.write("INSERT INTO biz_notice (id, title, content, type, priority, publisher_id, published_at, start_time, end_time, status, is_sticky, read_count, target_scope, department_id, attachment_url, created_at, updated_at, is_deleted) VALUES\n")
        
        vals = []
        for i in range(20):
            notice_id = 2400000000000000001 + i
            publisher_id = 1800000000000000001 + (i % 10)
            
            if i == 0 or i == 1:
                status = 0
                published_at = "NULL"
                start_time = "NULL"
            elif i < 17:
                status = 1
                pub_date = now - timedelta(days=30-i)
                published_at = f"'{format_dt(pub_date)}'"
                start_time = f"'{format_dt(pub_date)}'"
            elif i < 19:
                status = 2
                pub_date = now - timedelta(days=20-i%10)
                published_at = f"'{format_dt(pub_date)}'"
                start_time = f"'{format_dt(pub_date)}'"
            else:
                status = 0
                published_at = "NULL"
                future_date = now + timedelta(days=30)
                start_time = f"'{format_dt(future_date)}'"
            
            notice_type = (i % 3) + 1
            priority = (i % 3) + 1
            is_sticky = 1 if i % 5 == 0 else 0
            read_count = i * 10
            target_scope = i % 4
            dept_id = "NULL" if i % 2 == 0 else str(1700000000000000001 + (i % 10))
            attachment = "NULL" if i % 3 != 0 else f"'notice/2026/04/attachment_{i+1}.pdf'"
            create_date = now - timedelta(days=40-i)
            
            vals.append(
                f"({notice_id}, '通知公告{i+1:02d}', '<p>这是第{i+1}条公告的内容...</p>', "
                f"{notice_type}, {priority}, {publisher_id}, {published_at}, {start_time}, NULL, "
                f"{status}, {is_sticky}, {read_count}, {target_scope}, {dept_id}, {attachment}, "
                f"'{format_dt(create_date)}', '{format_dt(create_date)}', 0)"
            )
        f.write(",\n".join(vals) + ";\n\n")
        
        # 统计信息
        f.write("-- ============================================================================\n")
        f.write("-- 数据统计汇总\n")
        f.write("-- ============================================================================\n")
        f.write(f"-- 院系: {DEPT_COUNT}个\n")
        f.write(f"-- 用户: {SYS_ADMIN_COUNT + DEPT_ADMIN_COUNT + TEACHER_COUNT + STUDENT_COUNT}个\n")
        f.write(f"--   - 系统管理员: {SYS_ADMIN_COUNT}个\n")
        f.write(f"--   - 院系管理员: {DEPT_ADMIN_COUNT}个\n")
        f.write(f"--   - 教师: {TEACHER_COUNT}个\n")
        f.write(f"--   - 学生: {STUDENT_COUNT}个\n")
        f.write(f"-- 题目: {TOPIC_COUNT}个\n")
        f.write(f"-- 选题: {SELECTION_COUNT}个\n")
        f.write(f"-- 文档: {DOCUMENT_COUNT}个\n")
        f.write(f"-- 成绩: {GRADE_COUNT}个\n")
        f.write(f"-- 公告: {NOTICE_COUNT}个\n")
        f.write("-- ============================================================================\n")
        f.write("-- 生成时间: " + format_dt(now) + "\n")
        f.write("-- ============================================================================\n")
    
    print(f"✅ SQL文件生成成功: {OUTPUT_FILE}")
    print(f"📊 数据统计:")
    print(f"   - 院系: {DEPT_COUNT}个")
    print(f"   - 用户: {SYS_ADMIN_COUNT + DEPT_ADMIN_COUNT + TEACHER_COUNT + STUDENT_COUNT}个")
    print(f"   - 题目: {TOPIC_COUNT}个")
    print(f"   - 选题: {SELECTION_COUNT}个")
    print(f"   - 文档: {DOCUMENT_COUNT}个")
    print(f"   - 成绩: {GRADE_COUNT}个")
    print(f"   - 公告: {NOTICE_COUNT}个")

if __name__ == '__main__':
    main()
