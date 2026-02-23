# MyBatis-Plus封装方法使用分析

## 现有MyBatis-Plus内置方法分析

### 1. BaseMapper接口内置方法
MyBatis-Plus的BaseMapper<T>接口提供了丰富的CRUD方法：

```java
public interface BaseMapper<T> {
    // 基础CRUD
    int insert(T entity);                           // 插入
    int deleteById(Serializable id);               // 根据ID删除
    int deleteByMap(Map<String, Object> columnMap); // 根据Map条件删除
    int delete(Wrapper<T> wrapper);                // 根据条件删除
    int updateById(T entity);                      // 根据ID更新
    int update(T entity, Wrapper<T> updateWrapper); // 根据条件更新
    
    // 查询方法
    T selectById(Serializable id);                 // 根据ID查询
    List<T> selectBatchIds(Collection<? extends Serializable> idList); // 批量ID查询
    List<T> selectByMap(Map<String, Object> columnMap); // 根据Map查询
    T selectOne(Wrapper<T> queryWrapper);          // 查询一条记录
    Integer selectCount(Wrapper<T> queryWrapper);  // 查询总数
    List<T> selectList(Wrapper<T> queryWrapper);   // 查询列表
    List<Map<String, Object>> selectMaps(Wrapper<T> queryWrapper); // 查询Map列表
    List<Object> selectObjs(Wrapper<T> queryWrapper); // 查询对象列表
    
    // 分页查询（需要分页插件支持）
    <E extends IPage<T>> E selectPage(E page, Wrapper<T> queryWrapper);
    <E extends IPage<Map<String, Object>>> E selectMapsPage(E page, Wrapper<T> queryWrapper);
}
```

### 2. IService接口增强方法
MyBatis-Plus还提供了IService接口，在BaseMapper基础上增加了更多实用方法：

```java
public interface IService<T> {
    // 批量操作
    boolean saveBatch(Collection<T> entityList);           // 批量保存
    boolean saveOrUpdateBatch(Collection<T> entityList);   // 批量保存或更新
    boolean removeByIds(Collection<?> idList);            // 批量删除
    boolean removeByMap(Map<String, Object> columnMap);   // 根据Map删除
    
    // 查询增强
    List<T> listByIds(Collection<? extends Serializable> idList); // 批量查询
    List<T> listByMap(Map<String, Object> columnMap);     // 根据Map查询
    T getOne(Wrapper<T> queryWrapper);                    // 查询一条
    T getOne(Wrapper<T> queryWrapper, boolean throwEx);   // 查询一条（异常控制）
    
    // 分页增强
    IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper); // 分页查询
    IPage<Map<String, Object>> pageMaps(IPage<T> page, Wrapper<T> queryWrapper); // 分页Map查询
}
```

## 我们的自定义方法与内置方法对照

### 1. 可以直接使用MyBatis-Plus内置方法的场景

**✅ 批量ID查询**
```java
// 直接使用BaseMapper的内置方法
List<BizGrade> grades = bizGradeMapper.selectBatchIds(Arrays.asList(1L, 2L, 3L));

// 替代我们自定义的selectBatchWithOrder（如果不需要特定排序）
```

**✅ 条件查询**
```java
// 直接使用BaseMapper的内置方法
LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(BizGrade::getStatus, 1);
List<BizGrade> grades = bizGradeMapper.selectList(wrapper);

// 替代自定义的selectByXXX方法
```

**✅ 分页查询**
```java
// 直接使用BaseMapper + 分页插件
IPage<BizGrade> page = new Page<>(1, 10);
LambdaQueryWrapper<BizGrade> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(BizGrade::getStatus, 1);
IPage<BizGrade> result = bizGradeMapper.selectPage(page, wrapper);
```

### 2. 需要自定义扩展的场景

**❌ 关联查询优化（N+1问题）**
```java
// MyBatis-Plus没有内置的关联查询优化
// 必须自定义selectDetailsWithRelations方法
List<Map<String, Object>> details = bizGradeMapper.selectDetailsWithRelations(gradeIds);
```

**❌ 复杂统计查询**
```java
// MyBatis-Plus的count只能做简单统计
// 复杂统计需要自定义selectStatistics方法
Map<String, Object> stats = bizGradeMapper.selectStatistics(condition);
```

**❌ 保持顺序的批量查询**
```java
// BaseMapper.selectBatchIds不保证返回顺序
// 需要自定义selectBatchWithOrder方法来保持ID顺序
List<BizGrade> grades = bizGradeMapper.selectBatchWithOrder(orderedIds);
```

## 最佳实践建议

### 1. 优先使用MyBatis-Plus内置方法
```java
@Service
public class GradeServiceImpl extends ServiceImpl<BizGradeMapper, BizGrade> {
    
    // ✅ 优先使用IService提供的批量方法
    public void batchSaveGrades(List<BizGrade> grades) {
        this.saveBatch(grades);  // 使用IService内置方法
    }
    
    // ✅ 优先使用BaseMapper的基础查询
    public BizGrade getGradeById(Long id) {
        return this.getById(id);  // 使用IService内置方法
    }
    
    // ✅ 优先使用分页插件
    public IPage<BizGrade> getGradePage(int current, int size) {
        IPage<BizGrade> page = new Page<>(current, size);
        return this.page(page, Wrappers.emptyWrapper());
    }
}
```

### 2. 合理扩展自定义方法
```java
// 在MyBaseMapper中只添加真正需要的自定义方法
public interface MyBaseMapper<T> extends BaseMapper<T> {
    
    // ✅ 保留：解决N+1查询问题
    List<Map<String, Object>> selectDetailsWithRelations(@Param("ids") List<Long> ids);
    
    // ⚠️ 考虑：是否真的需要保持顺序？
    // List<T> selectBatchWithOrder(@Param("ids") List<Long> ids);
    
    // ✅ 保留：复杂统计需求
    Map<String, Object> selectStatistics(@Param("condition") Map<String, Object> condition);
}
```

### 3. 业务Mapper精简策略
```java
// 精简后的BizGradeMapper
public interface BizGradeMapper extends MyBaseMapper<BizGrade> {
    // 继承所有通用方法
    // 只添加真正特有的业务方法
}
```

## 性能对比分析

| 场景 | MyBatis-Plus内置方法 | 自定义方法 | 推荐选择 |
|------|---------------------|------------|----------|
| 简单CRUD | ✅ 完全支持 | ❌ 冗余 | ✅ 内置方法 |
| 批量ID查询 | ✅ 支持(selectBatchIds) | ⚠️ 可替代 | ✅ 内置方法 |
| 条件查询 | ✅ 强大(Wrapper) | ❌ 不必要 | ✅ 内置方法 |
| 分页查询 | ✅ 完美支持 | ❌ 冗余 | ✅ 内置方法 |
| 关联查询(N+1) | ❌ 不支持 | ✅ 专门优化 | ✅ 自定义方法 |
| 复杂统计 | ⚠️ 有限支持 | ✅ 灵活定制 | ✅ 自定义方法 |
| 保持顺序查询 | ❌ 不保证 | ✅ 明确支持 | ✅ 自定义方法 |

## 结论

**我们应该：**
1. ✅ **最大化使用MyBatis-Plus内置方法** - 减少重复代码，提高开发效率
2. ✅ **精简自定义方法** - 只保留解决特定问题的扩展方法
3. ✅ **统一标准** - 建立清晰的使用规范，避免混乱

**可以优化的方向：**
1. 移除不必要的`selectBatchWithOrder`方法（除非确实需要保持顺序）
2. 进一步利用MyBatis-Plus的Wrapper条件构造器
3. 统一使用IService接口提供的增强方法

这样的设计既充分发挥了MyBatis-Plus的强大能力，又保持了必要的灵活性！