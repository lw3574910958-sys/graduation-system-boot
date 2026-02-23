# MyBatis-Plus通用Mapper优化方案

## 方案概述
基于MyBatis-Plus的封装能力，通过扩展MyBaseMapper基类，提供通用的批量查询和性能优化方法，让所有业务Mapper都能共享这些优化能力。

## 核心设计理念

### 1. 统一接口设计
```java
public interface MyBaseMapper<T> extends BaseMapper<T> {
    // 通用批量查询详情及关联信息
    List<Map<String, Object>> selectDetailsWithRelations(@Param("ids") List<Long> ids);
    
    // 增强版批量查询（支持排序）
    List<T> selectBatchWithOrder(@Param("ids") List<Long> ids);
    
    // 通用统计方法
    Map<String, Object> selectStatistics(@Param("condition") Map<String, Object> condition);
}
```

### 2. 继承使用模式
```java
// 业务Mapper只需继承基类即可获得通用能力
public interface BizGradeMapper extends MyBaseMapper<BizGrade> {
    // 无需重复定义通用方法
    // 可添加特定业务方法
}
```

## 已实现的优化

### 1. BizGradeMapper通用化改造
**改造前**：
```java
// 需要单独定义方法
List<Map<String, Object>> selectGradeDetailsWithRelations(@Param("gradeIds") List<Long> gradeIds);
```

**改造后**：
```java
// 直接继承通用方法
public interface BizGradeMapper extends MyBaseMapper<BizGrade> {
    // selectDetailsWithRelations 已在基类中定义
}
```

### 2. XML实现统一化
所有Mapper的XML实现都遵循相同的模式：

```xml
<!-- 实现通用批量查询详情 -->
<select id="selectDetailsWithRelations" resultType="map">
    SELECT 
        t.id,
        t.field1,
        u.real_name as user_name,  <!-- 关联字段 -->
        d.name as dept_name        <!-- 关联字段 -->
    FROM your_table t
    LEFT JOIN sys_user u ON t.user_id = u.id
    LEFT JOIN sys_department d ON t.dept_id = d.id
    WHERE t.id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    AND t.is_deleted = 0
</select>
```

## 性能优化优势

### 1. 减少重复代码
- ❌ 之前：每个Mapper都要重复定义相似的批量查询方法
- ✅ 现在：通过继承获得通用能力，代码复用率大幅提升

### 2. 统一性能优化标准
```java
// 使用MyBatis-Plus原生方法 + 通用优化
@Override
public IPage<GradeVO> getGradePage(GradePageQueryDTO queryDTO) {
    // 1. 基础分页查询（MyBatis-Plus）
    IPage<BizGrade> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
    IPage<BizGrade> gradePage = bizGradeMapper.selectPage(page, wrapper);
    
    // 2. 批量获取关联信息（通用方法）
    List<Long> gradeIds = gradePage.getRecords().stream()
        .map(BizGrade::getId)
        .collect(Collectors.toList());
    List<Map<String, Object>> details = bizGradeMapper.selectDetailsWithRelations(gradeIds);
    
    // 3. 内存组装（避免N+1查询）
    // ...
}
```

### 3. 更好的维护性
- 所有批量查询遵循统一的命名规范
- SQL结构标准化，便于维护和优化
- 新增Mapper自动获得优化能力

## 使用示例

### 1. 通用批量查询
```java
// 查询多个成绩记录及其关联信息
List<Long> gradeIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
List<Map<String, Object>> details = bizGradeMapper.selectDetailsWithRelations(gradeIds);
```

### 2. 增强版批量查询（保持顺序）
```java
// 按指定顺序返回结果
List<Long> orderedIds = Arrays.asList(3L, 1L, 4L, 2L, 5L);
List<BizGrade> grades = bizGradeMapper.selectBatchWithOrder(orderedIds);
// 结果顺序与输入ID顺序一致
```

### 3. 通用统计查询
```java
// 动态条件统计
Map<String, Object> condition = new HashMap<>();
condition.put("minScore", 80);
condition.put("maxScore", 100);
Map<String, Object> stats = bizGradeMapper.selectStatistics(condition);
// 返回符合条件的成绩统计信息
```

## 扩展建议

### 1. 其他Mapper的通用化
建议将以下Mapper也改造为继承MyBaseMapper：
- ✅ BizDocumentMapper
- ✅ BizSelectionMapper  
- ✅ BizTopicMapper
- ⬜ SysUserMapper
- ⬜ SysDepartmentMapper

### 2. 更多通用方法
可考虑添加：
```java
// 批量软删除
int deleteBatchSoft(@Param("ids") List<Long> ids);

// 批量更新状态
int updateBatchStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

// 条件批量查询
List<T> selectByConditions(@Param("conditions") Map<String, Object> conditions);
```

### 3. 性能监控
建议添加：
- 方法执行时间监控
- 查询结果集大小统计
- 慢查询预警机制

这种基于MyBatis-Plus封装的通用Mapper方案，既充分利用了框架能力，又提供了良好的扩展性和维护性，是企业级应用的理想选择！