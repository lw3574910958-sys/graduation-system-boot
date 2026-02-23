# BasePageQueryDTO未使用方法清理说明

## 清理背景
在代码审查过程中发现`BasePageQueryDTO`类中的`getOffset()`和`getLimit()`方法从未被使用，属于冗余代码，按照项目重构规范进行清理。

## 清理内容

### 删除的未使用方法

1. **`getOffset()` 方法**
   - 功能：计算分页偏移量 `(current - 1) * size`
   - 删除原因：项目使用MyBatis-Plus的`Page`对象自动处理分页参数，无需手动计算偏移量
   - 替代方案：MyBatis-Plus `new Page<>(current, size)` 自动计算

2. **`getLimit()` 方法**  
   - 功能：返回每页大小 `size`
   - 删除原因：直接使用`getSize()`方法即可获取每页大小
   - 替代方案：直接访问`size`属性或使用继承的`getSize()`方法

## 项目分页实现方式

项目采用MyBatis-Plus标准分页模式：
```java
// 服务层分页实现示例
@Override
public IPage<SomeVO> getPage(SomePageQueryDTO queryDTO) {
    // 构建查询条件
    LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
    // ... 查询条件设置
    
    // MyBatis-Plus自动处理分页参数
    IPage<Entity> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
    IPage<Entity> resultPage = mapper.selectPage(page, wrapper);
    
    // 转换为VO并返回
    return convertToVOPage(resultPage);
}
```

MyBatis-Plus的`Page`构造函数会自动：
- 计算偏移量：`(current - 1) * size`
- 设置限制数量：`size`
- 处理边界情况和溢出控制

## 清理效果

1. **代码简洁性**：删除了18行冗余代码
2. **维护性提升**：避免了两种分页参数获取方式的混淆
3. **一致性保证**：统一使用MyBatis-Plus标准分页机制
4. **符合规范**：遵循"删除未使用代码"的重构原则

## 验证结果

- ✅ 编译通过，无语法错误
- ✅ 所有分页功能保持正常
- ✅ 项目分页逻辑不受影响
- ✅ 代码质量得到提升

## 注意事项

此次清理不会影响现有功能，因为：
1. 项目中所有分页都使用MyBatis-Plus标准方式
2. 没有任何地方调用过`getOffset()`和`getLimit()`方法
3. `current`和`size`属性仍然可用，通过继承的getter方法访问