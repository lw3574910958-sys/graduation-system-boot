# 高校毕业设计管理系统 - 后端

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.10-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis_Plus-3.5.15-blue.svg)](https://baomidou.com/)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

基于 Spring Boot 3 + DDD 领域驱动设计的高校毕业设计全流程管理系统后端服务

</div>

## 📖 项目简介

高校毕业设计管理系统后端服务，采用 Spring Boot 3.5.10 + Java 21 技术栈，基于 DDD（领域驱动设计）架构，覆盖题目发布、选题申请、文档审核、成绩评定等核心业务场景。系统支持系统管理员、院系管理员、教师、学生四种角色，实现毕业设计全流程数字化管理。

### ✨ 核心特性

- 🏗️ **DDD 架构**: 采用领域驱动设计，16 个 Maven 模块职责清晰
- 🔐 **安全可靠**: Sa-Token JWT 认证、BCrypt 加密、登录失败锁定、数据权限行级过滤
- 🚀 **性能优化**: Redis 分级缓存策略，缓存命中率 90%+
- 📊 **业务创新**: 名额预占机制、自动创建成绩记录、综合成绩自动计算
- 📝 **API 文档**: Knife4j + Swagger 自动生成，支持在线调试
- 🎯 **代码质量**: Lombok 简化代码、Hutool 工具集、统一异常处理

## 🛠️ 技术栈

### 核心框架
- **Java**: 21 (LTS 版本)
- **Spring Boot**: 3.5.10
- **Maven**: 3.8+ (多模块项目管理)

### 持久层
- **MyBatis-Plus**: 3.5.15 (数据访问层)
- **MySQL**: 8.0+ / 9.5.0 (utf8mb4 字符集)
- **Druid**: 1.2.27 (数据库连接池)
- **H2 Database**: 2.3.232 (开发测试)

### 认证授权
- **Sa-Token**: 1.44.0 (轻量级权限控制)
  - JWT Token 管理
  - 24h 有效期 + 30min 活跃超时
  - 自动刷新机制
- **Spring Security Crypto**: 7.0.2 (BCrypt 密码加密)

### 缓存
- **Redis**: 6.0+ (Lettuce 连接)
  - 用户信息缓存 (15min)
  - 院系信息缓存 (2h)
  - 仪表盘数据缓存 (5min)

### API 文档
- **Knife4j**: 4.5.0 (Swagger UI 增强)
- **Swagger Core**: 2.2.41 (OpenAPI 3.0)

### 工具库
- **Hutool**: 5.8.42 (Java 工具集)
- **Apache Commons Lang3**: 3.20.0
- **Jackson**: 2.20.1 (JSON 处理)
- **Lombok**: 1.18.42 (代码简化)

### 文件处理
- **Java NIO**: 本地文件存储
- **Kaptcha**: 2.3.2 (图形验证码)

### 开发辅助
- **Spring Boot DevTools**: 热部署
- **MapStruct**: 对象映射

## 📦 模块说明

系统采用多模块 Maven 架构，共 16 个模块：

```
graduation-system-boot/
├── graduation-bom/              # BOM 统一版本管理
├── graduation-common/           # 通用组件 (工具类、配置类、枚举类)
├── graduation-domain/           # 领域模型 (实体、值对象、领域服务)
├── graduation-infrastructure/   # 基础设施 (仓储实现、文件存储)
├── graduation-api/              # API 接口 (Controller、DTO、VO)
├── graduation-application/      # 全局启动与配置模块
├── graduation-auth/             # 认证授权模块
├── graduation-user/             # 用户管理模块
├── graduation-department/       # 院系管理模块
├── graduation-topic/            # 题目管理模块
├── graduation-selection/        # 选题管理模块
├── graduation-document/         # 文档管理模块
├── graduation-grade/            # 成绩管理模块
├── graduation-notice/           # 通知公告模块
├── graduation-dashboard/        # 仪表盘统计模块
├── graduation-log/              # 系统日志模块 (预留)
└── graduation-codegen/          # 代码生成模块
```

### 模块职责

| 模块名称 | 职责描述 | 核心内容 |
|---------|---------|---------|
| graduation-bom | 统一版本管理 | 依赖版本定义、BOM 导入 |
| graduation-common | 通用组件 | 工具类、配置类、枚举类、响应封装 |
| graduation-domain | 领域模型 | 实体类 (BizTopic/BizSelection/BizDocument 等) |
| graduation-infrastructure | 基础设施 | Mapper 接口、文件存储实现 |
| graduation-api | API 接口 | Controller、DTO、VO、WebSocket 配置 |
| graduation-auth | 认证授权 | Sa-Token 配置、登录逻辑、权限校验 |
| graduation-user | 用户管理 | 用户 CRUD、密码管理、头像上传 |
| graduation-topic | 题目管理 | 题目发布、审核、状态流转 |
| graduation-selection | 选题管理 | 选题申请、审核、名额预占 |
| graduation-document | 文档管理 | 文档上传、审核、顺序控制 |
| graduation-grade | 成绩管理 | 成绩录入、自动计算、等级评定 |
| graduation-notice | 通知公告 | 公告发布、撤回、阅读统计 |
| graduation-dashboard | 仪表盘 | 数据统计、图表分析 |

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选)

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/lw3574910958-sys/graduation-system-boot.git
cd graduation-system-boot
```

#### 2. 创建数据库

```sql
-- 创建数据库
CREATE DATABASE graduation_system DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导入表结构
USE graduation_system;
SOURCE sql/sys.sql;
```

#### 3. 修改配置文件

编辑 `graduation-application/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/graduation_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  
  # Redis 配置 (可选)
  data:
    redis:
      host: localhost
      port: 6379
      database: 1
      password: your_redis_password
```

#### 4. 编译项目

```bash
# Windows
mvnw clean install

# Linux/Mac
./mvnw clean install
```

#### 5. 启动项目

```bash
# 方式 1: Maven 启动
cd graduation-application
mvn spring-boot:run

# 方式 2: IDEA 启动
# 运行 GraduationSystemApplication.java
```

#### 6. 访问系统

- **API 文档**: http://localhost:8080/doc.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

## 📚 API 规范

### 响应格式

所有 API 统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  }
}
```

### 状态码

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 系统错误 |
| 1001-1999 | 用户相关错误 |
| 2001-2999 | 题目相关错误 |
| 3001-3999 | 选题相关错误 |

### 认证方式

使用 JWT Token 认证，在请求头中添加：

```
Authorization: Bearer {token}
```

Token 获取：调用 `/api/auth/login` 接口

## 🎯 核心业务机制

### 1. 名额预占机制

学生申请选题时立即占用名额，防止并发超选：

```java
// 名额预占
topic.setSelectedCount(topic.getSelectedCount() + 1);
bizTopicMapper.updateById(topic);

// 达到上限自动关闭
if (topic.getSelectedCount() >= topic.getMaxSelections()) {
    topic.setStatus(TopicStatus.CLOSED.getCode());
}
```

### 2. 审核教师自动赋值

选题申请自动路由到题目发布教师：

```java
// 自动设置 reviewer_id
Long reviewerId = bizTeacherService.getUserIdByTeacherId(topic.getTeacherId());
selection.setReviewerId(reviewerId);
```

### 3. 自动创建成绩记录

文档审核通过自动触发成绩记录创建：

```java
// 文档审核通过
if (reviewStatus == ReviewStatus.APPROVED.getCode()) {
    // 自动创建成绩记录
    gradeService.autoCreateGrade(selectionId, fileType);
}
```

### 4. 综合成绩自动计算

三项成绩齐全后自动加权计算：

```java
// 开题 30% + 中期 30% + 论文 40%
BigDecimal composite = proposal.getScore().multiply(new BigDecimal("0.3"))
    .add(midterm.getScore().multiply(new BigDecimal("0.3")))
    .add(thesis.getScore().multiply(new BigDecimal("0.4")));
```

### 5. 数据权限行级过滤

基于 DataPermissionUtil 动态拼接 SQL：

```java
// 院系管理员：只能看到本院系数据
@DataPermission(departmentId = true)
public List<TopicVO> getTopics() {
    // 自动追加 WHERE department_id = #{currentDepartmentId}
}
```

## 📊 数据库设计

### 核心表结构

系统包含 10 张核心表：

- **系统表** (2 张): sys_user, sys_department
- **业务表** (8 张): biz_student, biz_teacher, biz_admin, biz_topic, biz_selection, biz_document, biz_grade, biz_notice

### 关键索引设计

```sql
-- 函数索引：保证学生只能确认一个选题
UNIQUE KEY uk_student_confirmed_topic ((IF(status = 3, student_id, NULL)))

-- 复合索引：优化高频查询
KEY idx_department_status (department_id, status)
KEY idx_teacher_status (teacher_id, status)
```

详细设计请参考：[数据库设计文档](../doc/数据库设计文档.md)

## 🔧 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一异常处理 (BusinessException)
- 统一响应封装 (Result<T>)
- 日志规范 (SLF4J + Logback)

### 分层架构

```
API Layer (graduation-api)
    ↓
Application Layer (graduation-application)
    ↓
Domain Layer (graduation-domain)
    ↓
Infrastructure Layer (graduation-infrastructure)
```

### 添加新模块

1. 在父 pom.xml 中添加模块
2. 创建模块目录和 pom.xml
3. 实现实体类、Mapper、Service、Controller
4. 配置路由和权限

## 📝 业务逻辑文档

详细业务流程和状态机说明请参考：

- [业务逻辑与流程文档](../doc/业务逻辑与流程文档.md)
- [数据库设计文档](../doc/数据库设计文档.md)
- [项目介绍与技术架构](../doc/项目介绍与技术架构.md)

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 接口测试

使用 Knife4j 在线调试：http://localhost:8080/doc.html

## 📦 部署

### 生产环境配置

```yaml
spring:
  profiles: active: prod
  
server:
  port: 8080
  
logging:
  file:
    name: /var/log/graduation-system/app.log
```

### Docker 部署 (可选)

```dockerfile
FROM openjdk:21-jre-slim
COPY target/graduation-application-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 Apache License 2.0 协议，详见 [LICENSE](LICENSE) 文件。

## 👨‍💻 作者

- **lw** - [GitHub](https://github.com/lw3574910958-sys)
- 邮箱：lw3574910958@gmail.com

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [Sa-Token](https://sa-token.cc/)
- [Knife4j](https://doc.xiaominfo.com/)
- [Hutool](https://hutool.cn/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐ Star！**

</div>
