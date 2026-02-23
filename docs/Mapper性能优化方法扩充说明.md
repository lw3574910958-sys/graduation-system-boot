# Mapper性能优化方法扩充说明

## 优化背景
通过对各业务模块的分析，发现存在多个N+1查询问题和高频查询场景，需要通过自定义Mapper方法进行性能优化。

## 已完成的优化

### 1. BizGradeMapper优化
**新增方法**：
- `selectGradeDetailsWithRelations(List<Long> gradeIds)` - 批量查询成绩详情及关联信息

**优化效果**：
- 解决了GradeServiceImpl中的N+1查询问题
- 从N+1次数据库访问优化为1次批量查询
- 显著提升分页查询性能

### 2. BizDocumentMapper优化
**新增方法**：
- `selectDocumentDetailsWithRelations(List<Long> documentIds)` - 批量查询文档详情及关联信息

**优化场景**：
- 文档列表分页查询时的用户信息、题目信息、审核人信息获取
- 避免循环调用selectById造成的性能问题

### 3. BizSelectionMapper优化
**新增方法**：
- `selectByStudentId(Long studentId)` - 根据学生ID查询选题记录
- `selectByTeacherId(Long teacherId)` - 根据教师ID查询指导学生的选题
- `selectPendingReviews()` - 查询待审核的选题
- `selectSelectionDetailsWithRelations(List<Long> selectionIds)` - 批量查询选题详情及关联信息

**优化价值**：
- 提供常用的业务查询场景
- 解决选题列表查询时的N+1问题
- 支持教师端和学生端的不同查询需求

### 4. BizTopicMapper优化
**新增方法**：
- `selectByTeacherId(Long teacherId)` - 根据教师ID查询题目列表
- `selectOpenTopics()` - 查询开放状态的题目
- `selectTopicStatistics()` - 统计各状态题目数量
- `selectTopicDetailsWithRelations(List<Long> topicIds)` - 批量查询题目详情及关联信息

**业务价值**：
- 支持教师管理自己题目的场景
- 提供题目开放状态筛选
- 为管理后台提供统计数据支持

## 性能优化原理

### N+1查询问题解决
**优化前**（N+1查询）：
```java
// 循环查询，N次数据库访问
List<DocumentVO> vos = documents.stream()
    .map(doc -> {
        DocumentVO vo = new DocumentVO();
        vo.setUser(sysUserMapper.selectById(doc.getUserId()));     // N次
        vo.setTopic(bizTopicMapper.selectById(doc.getTopicId()));  // N次
        vo.setReviewer(sysUserMapper.selectById(doc.getReviewerId())); // N次
        return vo;
    })
    .collect(Collectors.toList());
```

**优化后**（批量查询）：
```java
// 一次批量查询获取所有关联信息
List<Map<String, Object>> details = bizDocumentMapper.selectDocumentDetailsWithRelations(documentIds);
Map<Long, Map<String, Object>> detailsMap = details.stream()
    .collect(Collectors.toMap(detail -> (Long) detail.get("id"), detail -> detail));

// 内存中组装VO，0次额外数据库访问
List<DocumentVO> vos = documents.stream()
    .map(doc -> {
        DocumentVO vo = new DocumentVO();
        Map<String, Object> detail = detailsMap.get(doc.getId());
        vo.setUserName((String) detail.get("user_name"));
        vo.setTopicTitle((String) detail.get("topic_title"));
        vo.setReviewerName((String) detail.get("reviewer_name"));
        return vo;
    })
    .collect(Collectors.toList());
```

### 性能对比
| 场景 | 优化前 | 优化后 | 性能提升 |
|------|--------|--------|----------|
| 10条记录查询 | 31次DB访问 | 1次DB访问 | 97%减少 |
| 50条记录查询 | 151次DB访问 | 1次DB访问 | 99%减少 |
| 100条记录查询 | 301次DB访问 | 1次DB访问 | 99.7%减少 |

## 使用建议

### 1. 批量查询方法使用场景
```java
// 适用于列表分页查询
@Override
public IPage<DocumentVO> getDocumentPage(DocumentPageQueryDTO queryDTO) {
    // 1. 获取基础数据
    IPage<BizDocument> page = bizDocumentMapper.selectPage(page, wrapper);
    
    // 2. 批量获取关联信息（关键优化点）
    List<Long> documentIds = page.getRecords().stream()
        .map(BizDocument::getId)
        .collect(Collectors.toList());
    List<Map<String, Object>> details = bizDocumentMapper.selectDocumentDetailsWithRelations(documentIds);
    
    // 3. 内存中组装最终结果
    // ...
}
```

### 2. 业务查询方法使用场景
```java
// 教师查询自己指导的选题
List<BizSelection> selections = bizSelectionMapper.selectByTeacherId(teacherId);

// 查询待审核的选题（管理员功能）
List<BizSelection> pendingReviews = bizSelectionMapper.selectPendingReviews();

// 获取题目统计信息
Map<String, Object> statistics = bizTopicMapper.selectTopicStatistics();
```

## 验证结果

- ✅ 所有新增方法编译通过
- ✅ Mapper接口与XML实现完全匹配
- ✅ SQL语法和参数绑定正确
- ✅ 与MyBatis-Plus现有方法无冲突

## 后续优化建议

### 1. 索引优化
建议为以下字段添加数据库索引：
- `biz_document.user_id`
- `biz_document.topic_id`  
- `biz_selection.student_id`
- `biz_selection.topic_id`
- `biz_topic.teacher_id`

### 2. 缓存策略
- 对于高频查询结果考虑引入Redis缓存
- 统计类查询结果可设置较长缓存时间
- 业务数据变更时及时清除相关缓存

### 3. 监控告警
- 监控批量查询方法的执行时间
- 设置慢查询告警阈值
- 定期分析查询性能趋势

这次性能优化显著提升了系统的查询效率，特别是在列表分页和批量数据展示场景下效果明显！