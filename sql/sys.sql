-- graduation_system.sql
-- 高校毕业设计论文管理系统 - 企业级数据库建表脚本(含 department_id 权限增强)
-- 字符集: utf8mb4 | 排序规则: utf8mb4_unicode_ci | 引擎: InnoDB

-- 创建数据库(可选)
CREATE DATABASE IF NOT EXISTS graduation_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE graduation_system;

-- ----------------------------
-- Table structure for sys_user
-- 系统统一用户表(所有角色共用)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号(学号/工号/管理员账号)',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(bcrypt加密)',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `user_type` VARCHAR(20) NOT NULL COMMENT '用户类型: student, teacher, admin',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL或存储路径',
  `last_login_at` DATETIME(3) NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(45) NULL DEFAULT NULL COMMENT '最后登录IP(支持IPv6)',
  `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `locked_until` DATETIME(3) NULL DEFAULT NULL COMMENT '账户锁定截止时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`),
  KEY `idx_last_login` (`last_login_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ----------------------------
-- Table structure for sys_department
-- 院系表
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(20) NOT NULL COMMENT '院系编码(如 CS001)',
  `name` VARCHAR(100) NOT NULL COMMENT '院系名称',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院系表';

-- ----------------------------
-- Table structure for biz_student
-- 学生业务信息表
-- ----------------------------
DROP TABLE IF EXISTS `biz_student`;
CREATE TABLE `biz_student` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关联 sys_user.id',
  `student_id` VARCHAR(20) NOT NULL COMMENT '学号',
  `department_id` BIGINT NOT NULL COMMENT '所属院系ID',
  `gender` TINYINT NOT NULL DEFAULT 1 COMMENT '性别: 0-女, 1-男',
  `major` VARCHAR(100) NOT NULL COMMENT '专业',
  `class_name` VARCHAR(50) NOT NULL COMMENT '班级',
  `phone` VARCHAR(20) NULL DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '邮箱',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_user_id` (`user_id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  KEY `idx_department` (`department_id`),
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_student_dept` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- ----------------------------
-- Table structure for biz_teacher
-- 教师业务信息表
-- ----------------------------
DROP TABLE IF EXISTS `biz_teacher`;
CREATE TABLE `biz_teacher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关联 sys_user.id',
  `teacher_id` VARCHAR(20) NOT NULL COMMENT '工号',
  `department_id` BIGINT NOT NULL COMMENT '所属院系ID',
  `gender` TINYINT NOT NULL DEFAULT 1 COMMENT '性别: 0-女, 1-男',
  `title` VARCHAR(50) NOT NULL COMMENT '职称(教授/副教授等)',
  `phone` VARCHAR(20) NULL DEFAULT NULL,
  `email` VARCHAR(100) NULL DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_user_id` (`user_id`),
  UNIQUE KEY `uk_teacher_id` (`teacher_id`),
  KEY `idx_department` (`department_id`),
  CONSTRAINT `fk_teacher_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_teacher_dept` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师表';

-- ----------------------------
-- Table structure for biz_admin
-- 管理员业务信息表
-- ----------------------------
DROP TABLE IF EXISTS `biz_admin`;
CREATE TABLE `biz_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关联 sys_user.id',
  `admin_id` VARCHAR(20) NOT NULL COMMENT '管理员编号',
  `department_id` BIGINT NULL DEFAULT NULL COMMENT '管理院系ID(NULL表示系统管理员)',
  `role_level` TINYINT NOT NULL DEFAULT 1 COMMENT '角色级别: 0-系统管理员, 1-院系管理员',
  `phone` VARCHAR(20) NULL DEFAULT NULL,
  `email` VARCHAR(100) NULL DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_id` (`user_id`),
  UNIQUE KEY `uk_admin_id` (`admin_id`),
  KEY `idx_department` (`department_id`),
  CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_admin_dept` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ----------------------------
-- Table structure for biz_topic
-- 毕业设计题目表
-- ----------------------------
DROP TABLE IF EXISTS `biz_topic`;
CREATE TABLE `biz_topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '题目标题',
  `description` TEXT NOT NULL COMMENT '题目描述',
  `teacher_id` BIGINT NOT NULL COMMENT '发布教师ID(biz_teacher.id)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-开放, 1-已选, 2-关闭',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_topic_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `biz_teacher` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- ----------------------------
-- Table structure for biz_selection
-- 学生选题记录表
-- ----------------------------
DROP TABLE IF EXISTS `biz_selection`;
CREATE TABLE `biz_selection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT '学生ID(biz_student.id)',
  `topic_id` BIGINT NOT NULL COMMENT '题目ID(biz_topic.id)',
  `topic_title` VARCHAR(200) NOT NULL COMMENT '选题时的题目标题快照',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待确认, 1-已确认',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_topic` (`topic_id`),
  -- ⚠️ MySQL 8.0+ 函数索引：确保一个学生只能有一个已确认的选题
  UNIQUE KEY `uk_student_confirmed_topic` ((IF(`status` = 1, `student_id`, NULL))),
  CONSTRAINT `fk_selection_student` FOREIGN KEY (`student_id`) REFERENCES `biz_student` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_selection_topic` FOREIGN KEY (`topic_id`) REFERENCES `biz_topic` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选题记录表';

-- ----------------------------
-- Table structure for biz_document
-- 文档上传表
-- ----------------------------
DROP TABLE IF EXISTS `biz_document`;
CREATE TABLE `biz_document` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '上传人ID(sys_user.id)',
  `topic_id` BIGINT NOT NULL COMMENT '关联题目ID',
  `file_type` TINYINT NOT NULL COMMENT '文件类型: 0-开题报告, 1-中期报告, 2-毕业论文',
  `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `stored_path` VARCHAR(500) NOT NULL COMMENT '服务器存储路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `review_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审, 1-通过, 2-驳回',
  `reviewed_at` DATETIME(3) NULL DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` BIGINT NULL DEFAULT NULL COMMENT '审核人ID(sys_user.id)',
  `feedback` TEXT NULL DEFAULT NULL COMMENT '审核意见',
  `uploaded_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '上传时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_topic` (`topic_id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_review_status` (`review_status`),
  CONSTRAINT `fk_document_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_document_topic` FOREIGN KEY (`topic_id`) REFERENCES `biz_topic` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_document_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- ----------------------------
-- Table structure for biz_grade
-- 成绩表
-- ----------------------------
DROP TABLE IF EXISTS `biz_grade`;
CREATE TABLE `biz_grade` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT '学生ID(biz_student.id)',
  `topic_id` BIGINT NOT NULL COMMENT '题目ID(biz_topic.id)',
  `score` DECIMAL(5,2) NOT NULL COMMENT '成绩(0.00 ~ 100.00)',
  `grader_id` BIGINT NOT NULL COMMENT '评分教师ID(sys_user.id)',
  `comment` TEXT NULL DEFAULT NULL COMMENT '评语',
  `graded_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '评分时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_topic` (`student_id`, `topic_id`),
  KEY `idx_grader` (`grader_id`),
  CONSTRAINT `fk_grade_student` FOREIGN KEY (`student_id`) REFERENCES `biz_student` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_grade_topic` FOREIGN KEY (`topic_id`) REFERENCES `biz_topic` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_grade_grader` FOREIGN KEY (`grader_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩表';

-- ----------------------------
-- Table structure for sys_log
-- 系统操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '操作人ID',
  `user_type` VARCHAR(20) NOT NULL COMMENT '操作人类型',
  `operation` VARCHAR(200) NOT NULL COMMENT '操作描述',
  `ip_address` VARCHAR(45) NOT NULL COMMENT '操作IP',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';