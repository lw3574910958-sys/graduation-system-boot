# Graduation System Boot 项目整体架构分析报告

## 📊 项目概述

**项目名称**: 高校毕业设计论文管理系统 (Graduation System Boot)  
**项目版本**: 1.0.0  
**技术栈**: Spring Boot 3.5.10 + Java 21 + Maven  
**项目类型**: 多模块Maven聚合项目  
**开发人员**: lw (lw3574910958@gmail.com)

## 🏗️ 项目架构设计

### 整体架构模式
采用**六边形架构**（Hexagonal Architecture）模式，遵循领域驱动设计（DDD）理念：

```
┌─────────────────────────────────────────────────────────────┐
│                    graduation-application                   │
│                         (应用启动层)                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                        graduation-api                       │
│                        (API接口层)                           │
└─────────────────────────┼───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                     业务服务模块                            │
│  graduation-auth  graduation-user  graduation-topic        │
│  graduation-grade  graduation-document  graduation-notice   │
│  graduation-selection  graduation-department  graduation-log│
└─────────────────────────┼───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                   graduation-infrastructure                │
│                      (基础设施层)                            │
└─────────────────────────┼───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                     graduation-domain                       │
│                       (领域模型层)                            │
└─────────────────────────┼───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                     graduation-common                       │
│                      (通用组件层)                            │
└─────────────────────────────────────────────────────────────┘
```

## 📁 模块结构详解

### 1. 核心模块

#### graduation-application (应用启动模块)
- **职责**: Spring Boot应用启动入口
- **关键组件**: 
  - `GraduationApplication.java` - 主启动类
  - `application.yml` - 全局配置文件
- **特性**: 启用异步处理(@EnableAsync)和缓存(@EnableCaching)

#### graduation-domain (领域模型模块)
- **职责**: 核心业务领域模型定义
- **包结构**:
  - `entity/` - 实体类 (12个业务领域)
    - admin/, department/, document/, grade/, log/, notice/
    - role/, selection/, student/, teacher/, topic/, user/
  - `enums/` - 枚举类 (6个功能模块)
    - document/, grade/, notice/, permission/, status/, user/

#### graduation-infrastructure (基础设施模块)
- **职责**: 数据访问、外部服务集成等基础设施
- **关键组件**:
  - `mapper/` - MyBatis Mapper接口 (13个模块)
  - `storage/` - 文件存储服务
  - `config/` - 基础设施配置
  - `handler/` - 自定义处理器
- **特色**: 实现了统一的`MyBaseMapper`基类

#### graduation-api (API接口模块)
- **职责**: RESTful API接口定义
- **包结构**:
  - `controller/` - 控制器 (8个业务模块)
  - `dto/` - 数据传输对象 (8个业务模块)
  - `service/` - 接口定义 (9个业务模块)
  - `vo/` - 视图对象 (9个业务模块)
  - `config/` - API配置

#### graduation-common (通用组件模块)
- **职责**: 公共工具类、常量、异常处理等
- **组件分类**:
  - `annotation/` - 自定义注解
  - `base/` - 基础类
  - `config/` - 公共配置
  - `constant/` - 常量定义
  - `enums/` - 公共枚举
  - `exception/` - 异常处理
  - `response/` - 统一响应
  - `util/` - 工具类

### 2. 业务模块

#### graduation-auth (认证模块)
- **职责**: 用户认证、授权、验证码等
- **核心技术**: Sa-Token + BCrypt密码加密 + Kaptcha验证码

#### graduation-user (用户模块)
- **职责**: 用户管理、角色分配
- **关键服务**: UserService, UserRoleService

#### graduation-department (部门模块)
- **职责**: 院系管理、组织架构

#### graduation-topic (选题模块)
- **职责**: 毕业设计题目管理
- **状态流转**: 草稿 → 待审核 → 已发布 → 已关闭

#### graduation-selection (选题选择模块)
- **职责**: 学生选题、导师分配
- **状态管理**: 待确认 → 已确认 → 已取消

#### graduation-document (文档模块)
- **职责**: 文档管理、审核流程
- **文档类型**: 开题报告、中期报告、终稿等

#### graduation-grade (成绩模块)
- **职责**: 成绩录入、计算、统计分析
- **特色功能**: 自动计算、成绩分布统计、绩点计算

#### graduation-notice (通知公告模块)
- **职责**: 系统通知、公告发布
- **状态管理**: 草稿 → 已发布 → 已撤回

#### graduation-log (日志模块)
- **职责**: 系统操作日志、安全审计
- **核心技术**: AOP切面 + 自定义注解

### 3. 支撑模块

#### graduation-bom (物料清单模块)
- **职责**: 统一依赖版本管理

#### graduation-codegen (代码生成模块)
- **职责**: 代码自动生成工具

## ⚙️ 技术栈详情

### 核心框架
- **Spring Boot**: 3.5.10
- **Java版本**: 21
- **构建工具**: Maven 3.x

### 数据访问
- **ORM框架**: MyBatis-Plus 3.x
- **数据库**: MySQL 8.x
- **连接池**: Druid
- **缓存**: Redis (Lettuce客户端)

### 安全认证
- **认证框架**: Sa-Token
- **密码加密**: BCrypt
- **验证码**: Kaptcha

### API文档
- **OpenAPI**: SpringDoc OpenAPI 3
- **UI界面**: Knife4j (增强版Swagger)

### 文件存储
- **本地存储**: 支持
- **云存储**: MinIO, 阿里云OSS
- **统一配置**: FileStorageProperties

### 监控运维
- **数据库监控**: Druid StatViewServlet
- **日志管理**: Logback
- **性能监控**: 内置慢SQL监控

## 🔧 配置管理

### 环境配置
```yaml
# 应用配置
server.port: 8080
spring.application.name: graduation-system

# 数据源配置
spring.datasource.url: jdbc:mysql://localhost:3306/graduation_system
spring.datasource.username: ${DB_USERNAME:root}
spring.datasource.password: ${DB_PASSWORD:rctf1234}

# Redis配置
spring.data.redis.host: localhost
spring.data.redis.port: 6379
spring.data.redis.database: 1

# 文件存储配置
file.storage.base-path: ${UPLOAD_DIR:D:/Project/myapps/graduation-system/data/uploadFiles}
file.storage.url-prefix: /files
```

### 安全配置
```yaml
# Sa-Token配置
sa-token.token-name: user_token
sa-token.token-prefix: Bearer
sa-token.timeout: 86400  # 24小时
sa-token.active-timeout: 1800  # 30分钟活跃超时
```

## 📊 数据库设计

### 核心表结构
项目包含完整的数据库表设计，主要模块包括：

1. **用户管理**: 用户表、角色表、用户角色关联表
2. **院系管理**: 院系表、专业表
3. **选题管理**: 题目表、选题表、选题记录表
4. **文档管理**: 文档表、文档类型表、审核记录表
5. **成绩管理**: 成绩表、评分标准表
6. **通知公告**: 通知表、公告表
7. **系统日志**: 操作日志表、安全日志表
8. **系统管理**: 管理员表、配置表

### 特色设计
- **软删除**: 使用`is_deleted`字段实现逻辑删除
- **状态机**: 完整的状态流转设计
- **索引优化**: 关键字段建立合适索引
- **外键约束**: 保证数据一致性

## 🚀 项目特色

### 1. 模块化设计
- 严格遵循单一职责原则
- 清晰的层次划分和依赖关系
- 便于维护和扩展

### 2. 统一规范
- 统一的异常处理机制
- 标准化的响应格式
- 一致的命名规范

### 3. 性能优化
- Redis缓存热点数据
- MyBatis-Plus性能优化
- 数据库连接池调优

### 4. 安全保障
- 完整的权限控制体系
- 数据库防火墙配置
- 操作日志审计

### 5. 开发友好
- 完善的API文档
- 详细的代码注释
- 规范的项目结构

## 📈 项目现状

### 已完成功能
✅ 用户认证与授权  
✅ 院系组织管理  
✅ 毕业设计选题管理  
✅ 文档提交与审核  
✅ 成绩录入与统计  
✅ 通知公告发布  
✅ 系统操作日志  
✅ 文件存储管理  

### 技术特点
✅ 采用现代化Spring Boot 3.x技术栈  
✅ 实现完整的DDD分层架构  
✅ 集成丰富的第三方组件  
✅ 具备良好的可扩展性和维护性  

## 🎯 项目价值

这是一个完整的企业级应用系统，具备以下价值：

1. **教学实践价值**: 展示了现代Java企业级开发的最佳实践
2. **技术学习价值**: 涵盖了主流框架和技术的应用
3. **工程实践价值**: 体现了软件工程的规范化和标准化
4. **业务实用价值**: 可直接应用于高校毕业设计管理场景

该项目充分展现了开发者的技术实力和工程素养，是一个高质量的软件工程项目。