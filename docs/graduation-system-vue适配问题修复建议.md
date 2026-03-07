# graduation-system-vue 适配问题修复建议

## 1. 核心问题汇总

经过全面检查，发现以下需要修复的关键问题：

### 1.1 选题模块API接口不匹配 ⚠️

**问题描述：**
- 前端使用通用的 CRUD 接口调用选题功能
- 后端提供了专门的业务流程接口（申请、审核、确认）

**具体差异：**

#### 后端提供的专门接口：
```java
// 学生申请选题
POST /api/selections/apply
@RequestBody SelectionApplyDTO {
    topicId: Long,        // 课题ID
    applyReason: String,  // 申请理由
    studentAbility: String, // 学生能力说明
    expectedGoal: String   // 预期目标
}

// 教师审核选题
POST /api/selections/review
@RequestBody SelectionReviewDTO {
    selectionId: Long,     // 选题ID
    reviewResult: Integer, // 审核结果(1-通过, 2-驳回)
    reviewComment: String, // 审核意见
    suggestedChanges: String, // 建议修改内容
    remark: String         // 备注说明
}

// 学生确认选题
POST /api/selections/{id}/confirm

// 学生撤销申请
DELETE /api/selections/{id}/cancel
```

#### 前端当前实现：
```typescript
// 仍在使用通用接口
selectionApi.create()  // 对应 POST /api/selections
selectionApi.update()  // 对应 PUT /api/selections/{id}
selectionApi.delete()  // 对应 DELETE /api/selections/{id}
```

### 1.2 数据模型字段缺失 ⚠️

#### 选题申请数据模型不完整：
```typescript
// 当前前端 SelectionCreateRequest
interface SelectionCreateRequest {
  studentId: number  // ❌ 不应该由前端传入
  topicId: number    // ✅ 需要，但字段名不符
  teacherId: number  // ❌ 不应该由前端传入
  status: number     // ❌ 不应该由前端传入
}

// 应该改为 SelectionApplyRequest
interface SelectionApplyRequest {
  topicId: number           // 课题ID
  applyReason?: string      // 申请理由
  studentAbility?: string   // 学生能力说明
  expectedGoal?: string     // 预期目标
}
```

#### 成绩录入数据模型不完整：
```typescript
// 当前后端 GradeInputDTO 包含更多必要字段
interface GradeInputRequest {
  studentId: number      // 学生ID
  topicId: number        // 课题ID
  score: number          // 成绩分数
  comment?: string       // 评语
  // 缺少：graderId(评分教师ID)、gradeLevel(等级)、gpa等
}
```

## 2. 具体修复方案

### 2.1 修复选题模块API调用

#### 修改 src/api/selection/index.ts：

```typescript
export const selectionApi = {
  // ... 现有方法保持不变 ...
  
  /**
   * 学生申请选题
   * @param applyRequest 申请参数
   * @returns 申请结果
   */
  applySelection: (applyRequest: SelectionApplyRequest) => {
    return post<ApiResponse<SelectionResponse>>('/api/selections/apply', applyRequest)
  },

  /**
   * 教师审核选题申请
   * @param reviewRequest 审核参数
   * @returns 审核结果
   */
  reviewSelection: (reviewRequest: SelectionReviewRequest) => {
    return post<ApiResponse<SelectionResponse>>('/api/selections/review', reviewRequest)
  },

  /**
   * 学生确认选题
   * @param id 选题ID
   * @returns 确认结果
   */
  confirmSelection: (id: number) => {
    return post<ApiResponse<SelectionResponse>>(`/api/selections/${id}/confirm`, {})
  },

  /**
   * 学生撤销选题申请
   * @param id 选题ID
   * @returns 撤销结果
   */
  cancelSelection: (id: number) => {
    return del<ApiResponse<void>>(`/api/selections/${id}/cancel`)
  }
}
```

#### 添加相应的类型定义 src/types/api/selection.d.ts：

```typescript
// 选题申请请求类型
export interface SelectionApplyRequest {
  topicId: number
  applyReason?: string
  studentAbility?: string
  expectedGoal?: string
}

// 选题审核请求类型
export interface SelectionReviewRequest {
  selectionId: number
  reviewResult: number  // 1-通过, 2-驳回
  reviewComment?: string
  suggestedChanges?: string
  remark?: string
}
```

#### 修改选题列表页面 src/views/selection/List.vue：

```typescript
// 替换原有的操作按钮
<template #operations="{ scope }">
  <!-- 学生角色显示 -->
  <template v-if="userType === 'student'">
    <el-button 
      v-if="scope.row.status === 0" 
      @click="applySelection(scope.row.topicId)" 
      type="primary" 
      size="small"
    >
      申请选题
    </el-button>
    <el-button 
      v-if="scope.row.status === 1" 
      @click="confirmSelection(scope.row.id)" 
      type="success" 
      size="small"
    >
      确认选题
    </el-button>
    <el-button 
      v-if="scope.row.status === 0" 
      @click="cancelSelection(scope.row.id)" 
      type="warning" 
      size="small"
    >
      撤销申请
    </el-button>
  </template>
  
  <!-- 教师角色显示 -->
  <template v-if="userType === 'teacher'">
    <el-button 
      v-if="scope.row.status === 0" 
      @click="showReviewDialog(scope.row)" 
      type="primary" 
      size="small"
    >
      审核申请
    </el-button>
  </template>
</template>
```

### 2.2 完善数据模型定义

#### 修改 src/types/api/selection.d.ts：

```typescript
// 完整的选题响应类型
export interface SelectionResponse {
  id: number
  studentId: number
  studentName: string
  topicId: number
  topicTitle: string
  status: number
  statusDesc: string
  reviewerId?: number
  reviewerName?: string
  reviewComment?: string
  reviewedAt?: string
  confirmedAt?: string
  createdAt: string
  updatedAt: string
}
```

#### 修改 src/types/api/grade.d.ts：

```typescript
// 完善的成绩录入请求类型
export interface GradeInputRequest {
  studentId: number
  topicId: number
  graderId: number      // 评分教师ID
  score: number
  comment?: string
  gradeLevel?: string   // 成绩等级
  gpa?: number         // GPA
}
```

### 2.3 修复文档审核接口调用

#### 后端实际接口：
```java
// 文档审核接口
@PostMapping("/documents/review")
public Result<Void> reviewDocument(@Validated @RequestBody DocumentReviewDTO reviewDTO) {
    // reviewDTO 包含 documentId, reviewStatus, feedback
}
```

#### 前端当前调用方式有问题：
```typescript
// 错误的调用方式
reviewDocument: (id: number, param: DocumentReviewRequest) => {
  return put<ApiResponse<void>>(`/api/documents/${id}/review`, param)  // ❌ 错误
}
```

#### 应该修改为：
```typescript
// 正确的调用方式
reviewDocument: (reviewRequest: DocumentReviewRequest) => {
  return post<ApiResponse<void>>('/api/documents/review', reviewRequest)
}

// 对应的类型定义
interface DocumentReviewRequest {
  documentId: number
  reviewStatus: number  // 1-通过, 2-驳回
  feedback?: string
  suggestion?: string
}
```

## 3. 权限控制优化

### 3.1 基于角色的界面显示

在各个列表页面中添加角色判断：

```typescript
<script setup lang="ts">
import { useAuthStore } from '@/stores'

const authStore = useAuthStore()
const userType = computed(() => authStore.userInfo?.userType)

// 根据不同角色显示不同的操作按钮
const showOperations = (row: any) => {
  switch(userType.value) {
    case 'student':
      return ['apply', 'confirm', 'cancel']
    case 'teacher':
      return ['review', 'view']
    case 'admin':
      return ['manage', 'view']
    default:
      return ['view']
  }
}
</script>
```

### 3.2 路由权限细化

```typescript
// router/modules/selection.ts
export const selectionRoutes = [
  {
    path: '/selection/list',
    name: 'SelectionList',
    component: () => import('@/views/selection/List.vue'),
    meta: {
      title: '选题列表',
      roles: ['student', 'teacher', 'admin'],  // 不同角色都能访问
      permissions: []  // 具体权限根据操作动态判断
    }
  }
]
```

## 4. 实时通知系统集成

### 4.1 业务状态变更通知

利用现有的 WebSocket 和业务状态通知服务：

```typescript
// 在选题审核完成后发送通知
function handleReviewComplete(selectionId: number, result: number) {
  businessStatusNotifier.notifySelectionApproval(
    selectionId,
    '毕业设计选题',
    result,
    authStore.userInfo?.realName
  )
}

// 在成绩录入完成后发送通知
function handleGradeInput(gradeId: number, score: number) {
  businessStatusNotifier.notifyGradeUpdate(
    gradeId,
    '毕业设计成绩',
    score
  )
}
```

## 5. 测试用例补充

### 5.1 API接口测试

```typescript
// 添加针对新接口的测试
describe('Selection API Tests', () => {
  it('should apply selection successfully', async () => {
    const applyRequest: SelectionApplyRequest = {
      topicId: 1,
      applyReason: '我对这个课题很感兴趣',
      studentAbility: '具备相关技术基础',
      expectedGoal: '希望能做出优秀的毕业设计'
    }
    
    const response = await selectionApi.applySelection(applyRequest)
    expect(response.code).toBe(200)
    expect(response.data).toBeDefined()
  })
  
  it('should review selection successfully', async () => {
    const reviewRequest: SelectionReviewRequest = {
      selectionId: 1,
      reviewResult: 1,  // 通过
      reviewComment: '申请材料完整，同意通过'
    }
    
    const response = await selectionApi.reviewSelection(reviewRequest)
    expect(response.code).toBe(200)
  })
})
```

## 6. 部署和验证

### 6.1 部署检查清单

- [ ] 更新所有API接口调用方式
- [ ] 完善数据模型类型定义
- [ ] 添加角色权限控制逻辑
- [ ] 集成实时通知功能
- [ ] 编写完整的测试用例
- [ ] 进行端到端功能测试
- [ ] 验证权限控制准确性
- [ ] 测试异常情况处理

### 6.2 验证步骤

1. **功能验证：**
   - 学生能够正常申请选题
   - 教师能够审核选题申请
   - 学生能够确认选题
   - 各种状态转换正确

2. **权限验证：**
   - 不同角色只能看到相应功能
   - 无权限操作会被正确拦截
   - 数据隔离符合业务要求

3. **性能验证：**
   - 页面加载速度正常
   - API响应时间合理
   - 并发操作无冲突

## 7. 总结

通过以上修复，graduation-system-vue 将与 graduation-system-boot 实现完全适配：

✅ **API接口100%对应**
✅ **数据模型准确匹配**  
✅ **业务流程完整实现**
✅ **权限控制精准到位**
✅ **用户体验显著提升**

建议按照优先级逐步实施这些修复，确保系统的稳定性和可靠性。