# Mapper接口检查与扩充建议

## 检查发现的问题

### 1. 严重的功能性缺失

**BizGradeMapper缺少自定义查询方法**
- **问题**：GradeServiceImpl中调用了`bizGradeMapper.selectGradeDetailsWithRelations(gradeIds)`方法
- **现状**：BizGradeMapper接口中未定义此方法，对应的XML文件中也未实现
- **影响**：会导致运行时出现`NoSuchMethodException`异常

**需要添加的方法**：
```java
public interface BizGradeMapper extends MyBaseMapper<BizGrade> {
    /**
     * 批量查询成绩详情及关联信息（优化N+1查询问题）
     * 
     * @param gradeIds 成绩ID列表
     * @return 包含成绩详情和关联信息的结果列表
     */
    List<Map<String, Object>> selectGradeDetailsWithRelations(@Param("gradeIds") List<Long> gradeIds);
}
```

### 2. Mapper接口结构分析

**正确的Mapper结构**：
✅ `MyBaseMapper<T>` - 自定义基础Mapper接口
✅ 各业务Mapper继承MyBaseMapper，获得通用CRUD方法
✅ 空接口设计符合MyBatis-Plus规范

**存在的Mapper接口**：
- ✅ SysUserMapper - 系统用户
- ✅ BizDocumentMapper - 文档管理  
- ✅ BizTopicMapper - 题目管理
- ✅ BizGradeMapper - 成绩管理
- ✅ BizSelectionMapper - 选题管理
- ✅ BizStudentMapper - 学生管理
- ✅ BizTeacherMapper - 教师管理
- ✅ BizNoticeMapper - 通知管理
- ✅ SysDepartmentMapper - 院系管理
- ✅ SysUserRoleMapper - 用户角色
- ✅ BizAdminMapper - 管理员
- ✅ SysLogMapper - 系统日志

### 3. 需要扩充的自定义方法

#### BizGradeMapper需要扩充
```xml
<!-- BizGradeMapper.xml -->
<select id="selectGradeDetailsWithRelations" resultType="map">
    SELECT 
        g.id,
        g.student_id,
        g.topic_id,
        g.score,
        g.grader_id,
        g.comment,
        g.graded_at,
        s.student_id as student_number,
        u.real_name as student_name,
        t.title as topic_title,
        ug.real_name as grader_name
    FROM biz_grade g
    LEFT JOIN biz_student s ON g.student_id = s.id
    LEFT JOIN sys_user u ON s.user_id = u.id
    LEFT JOIN biz_topic t ON g.topic_id = t.id
    LEFT JOIN sys_user ug ON g.grader_id = ug.id
    WHERE g.id IN
    <foreach collection="gradeIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    AND g.is_deleted = 0
</select>
```

#### BizTopicMapper可能需要扩充
```java
public interface BizTopicMapper extends MyBaseMapper<BizTopic> {
    /**
     * 根据教师ID查询题目列表
     */
    List<BizTopic> selectByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查询开放状态的题目
     */
    List<BizTopic> selectOpenTopics();
    
    /**
     * 统计各状态题目数量
     */
    Map<String, Object> selectTopicStatistics();
}
```

#### BizSelectionMapper可能需要扩充
```java
public interface BizSelectionMapper extends MyBaseMapper<BizSelection> {
    /**
     * 根据学生ID查询选题记录
     */
    List<BizSelection> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 根据教师ID查询指导学生的选题
     */
    List<BizSelection> selectByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 查询待审核的选题
     */
    List<BizSelection> selectPendingReviews();
}
```

## 建议的改进措施

### 1. 立即修复
- ✅ 为BizGradeMapper添加`selectGradeDetailsWithRelations`方法及XML实现
- ✅ 验证所有Mapper接口与XML文件的对应关系

### 2. 性能优化建议
- ⚠️ 识别N+1查询问题，添加批量查询方法
- ⚠️ 为高频查询场景添加索引优化的自定义方法
- ⚠️ 考虑添加分页查询的优化版本

### 3. 代码质量提升
- ⚠️ 统一Mapper方法命名规范
- ⚠️ 添加详细的JavaDoc注释
- ⚠️ 建立Mapper方法使用规范文档

## 验证方法

### 编译验证
```bash
mvn compile
```

### 运行时验证
```bash
# 启动应用，测试相关功能
mvn spring-boot:run
```

### 单元测试建议
为每个Mapper添加对应的单元测试，验证自定义方法的正确性。

## 总结

当前Mapper架构基本正确，但存在关键的功能性缺失。建议优先修复BizGradeMapper的自定义查询方法，然后逐步完善其他Mapper的性能优化方法。