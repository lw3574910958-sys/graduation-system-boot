# 头像路径缺少 /files 前缀问题修复说明

## 问题描述

访问用户列表时，后端日志报错：

```
org.springframework.web.servlet.resource.NoResourceFoundException: 
No static resource avatar/2011317294235017217/avatar.jpg.
```

前端请求头像图片时，浏览器 Network 面板显示请求地址为：
```
http://localhost:8080/avatar/2011317294235017217/avatar.jpg
```

**问题**：URL 缺少 `/files` 前缀，正确的应该是：
```
http://localhost:8080/files/avatar/2011317294235017217/avatar.jpg
```

## 问题根源分析

### 1. 后端配置

**LocalFileStorageServiceImpl**（文件存储服务）：
```java
@Value("${file.storage.url-prefix:/files}")
private String urlPrefix;

@Override
public String getUrl(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
        return null;
    }
    return urlPrefix + "/" + filePath;  // 返回：/files/avatar/用户 ID/avatar.jpg
}
```

**WebConfig**（静态资源映射）：
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final FileStorageProperties fileStorageProperties;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String fileSystemPath = Paths.get(fileStorageProperties.getBasePath()).toAbsolutePath().toString();
        registry.addResourceHandler(fileStorageProperties.getUrlPrefix() + "/**")
                .addResourceLocations("file:" + fileSystemPath + "/");
    }
}
```

**配置说明**：
- `urlPrefix` 配置为 `/files`
- 静态资源处理器映射：`/files/**` → `file:D:/Project/myapps/graduation-system/data/uploadFiles/`
- 只有访问 `/files/xxx` 路径时，Spring Boot 才会从指定目录读取文件

### 2. 数据库存储

数据库中 `sys_user` 表的 `avatar` 字段存储的是：
```
avatar/2011317294235017217/avatar.jpg
```

**注意**：没有 `/files` 前缀，因为这是文件在上传目录中的相对路径。

### 3. 前端处理流程

#### 上传阶段（正确）
```typescript
// FileUpload.vue
const fileUrl = response.data.storedPath || response.data.url || response.data.name
existingFile.url = fileUrl  // 保存：avatar/用户 ID/avatar.jpg
```

✅ 这个阶段保存相对路径是正确的。

#### 显示阶段（错误 - 修复前）
```typescript
// utils.ts - urls2FileList() - 修复前
export const urls2FileList = (url: string | null | undefined): FileItem[] => {
  return url
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item)
    .map((item) => ({
      name: item,
      url: item.startsWith('/') ? normalizePath(constants.BASE_URL, item) : item,
      // ❌ 问题：如果 item 不以 / 开头，直接返回原值，不添加 BASE_URL
    }))
}
```

**问题分析**：
1. 数据库中的路径是 `avatar/xxx.jpg`（不以 `/` 开头）
2. `item.startsWith('/')` 返回 `false`
3. 直接返回 `item` 原值，不调用 `normalizePath()`
4. 最终 URL 就是 `avatar/xxx.jpg`，没有添加 `BASE_URL`（`http://localhost:8080`）
5. 浏览器发起请求：`http://localhost:5173/avatar/xxx.jpg`（开发环境）或 `http://localhost:8080/avatar/xxx.jpg`
6. Spring Boot 找不到 `/avatar/**` 的资源映射，抛出 `NoResourceFoundException`

## 修复方案

### 核心思路

在显示阶段（从数据库读取后），为所有路径添加 `/files` 前缀，确保可以匹配到后端的静态资源处理器。

### 修改内容

**修改文件**：`graduation-system-vue/src/utils/utils.ts`

**修复后的代码**：
```typescript
export const urls2FileList = (url: string | null | undefined): FileItem[] => {
  if (!url || typeof url !== 'string') {
    return []
  }
  return url
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item)
    .map((item) => ({
      name: item,
      // ✅ 正确：根据路径格式智能添加 /files 前缀
      url: item.startsWith('/files') 
        ? normalizePath(constants.BASE_URL, item)           // 已有 /files，直接使用
        : item.startsWith('/')
          ? normalizePath(constants.BASE_URL, '/files' + item)  // 有 / 但没有 /files，添加 /files
          : normalizePath(constants.BASE_URL, '/files/' + item),  // 没有 /，添加 /files/
    }))
}
```

### 处理逻辑

| 数据库中的路径 | 判断条件 | 最终 URL |
|--------------|---------|---------|
| `avatar/xxx.jpg` | 不以 `/` 开头 | `http://localhost:8080/files/avatar/xxx.jpg` |
| `/avatar/xxx.jpg` | 以 `/` 开头但不以 `/files` 开头 | `http://localhost:8080/files/avatar/xxx.jpg` |
| `/files/avatar/xxx.jpg` | 以 `/files` 开头 | `http://localhost:8080/files/avatar/xxx.jpg` |

## 完整流程对比

### 修复前

```
数据库读取：avatar/2011317294235017217/avatar.jpg
  ↓
urls2FileList() 处理
  ↓
不以 / 开头，直接返回原值
  ↓
Avatar 组件收到：avatar/2011317294235017217/avatar.jpg
  ↓
浏览器请求：http://localhost:8080/avatar/2011317294235017217/avatar.jpg
  ↓
❌ Spring Boot 找不到 /avatar/** 映射，抛出异常
```

### 修复后

```
数据库读取：avatar/2011317294235017217/avatar.jpg
  ↓
urls2FileList() 处理
  ↓
不以 / 开头，添加 /files/ 前缀
  ↓
Avatar 组件收到：http://localhost:8080/files/avatar/2011317294235017217/avatar.jpg
  ↓
浏览器请求：http://localhost:8080/files/avatar/2011317294235017217/avatar.jpg
  ↓
✅ Spring Boot 匹配到 /files/** 映射，从文件目录读取并返回图片
```

## my-admin 参考实现

通过对比 my-admin 项目的实现，发现两者的处理方式一致：

### my-admin 的 FileConfig
```java
@Configuration
public class FileConfig implements WebMvcConfigurer {
    @Value("${file.path}")
    private String path;  // 配置为 "upload"
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/" + path + "/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
```

### my-admin 的 utils.ts
```typescript
export const getFileUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http')) {
    return url
  }
  return `${constants.BASE_URL}/${url}`
}
```

**关键差异**：
- my-admin 的配置是 `/upload/**`，数据库中存储的也是完整路径
- graduation-system 的配置是 `/files/**`，但数据库中只存储了相对路径
- 因此需要在前端显示时动态添加 `/files` 前缀

## 测试验证

### 1. 启动服务
```bash
# 后端
cd graduation-system-boot
mvn spring-boot:run

# 前端
cd graduation-system-vue
pnpm run dev
```

### 2. 访问用户列表
打开浏览器访问：`http://localhost:5173/user`

### 3. 检查 Network 面板

**预期结果**：
- ✅ 头像图片请求地址：`http://localhost:8080/files/avatar/用户 ID/avatar.jpg`
- ✅ 状态码：`200 OK`
- ✅ 响应类型：`image/jpeg`
- ✅ 头像正常显示

### 4. 检查后端日志

**预期结果**：
- ✅ 不再出现 `NoResourceFoundException` 异常
- ✅ 没有 404 错误

## 相关文件清单

### 前端文件（已修改）
- ✅ `graduation-system-vue/src/utils/utils.ts` - urls2FileList() 函数

### 后端文件（无需修改）
- `graduation-common/src/main/java/com/lw/graduation/common/config/FileStorageProperties.java`
- `graduation-infrastructure/src/main/java/com/lw/graduation/infrastructure/storage/impl/LocalFileStorageServiceImpl.java`
- `graduation-api/src/main/java/com/lw/graduation/api/config/WebConfig.java`

## 总结

本次修复遵循了**"数据库存储相对路径，显示时转换为完整 URL"**的原则，通过在 `urls2FileList()` 函数中智能添加 `/files` 前缀，确保了：

1. **兼容性**：无论数据库中存储的路径格式如何，都能正确转换为完整 URL
2. **环境无关性**：自动使用当前环境的 `BASE_URL`
3. **可维护性**：集中在一处处理，便于后续维护

**核心修复点**：
- ✅ 识别路径是否以 `/files` 开头
- ✅ 对于不以 `/files` 开头的路径，自动添加 `/files` 前缀
- ✅ 使用 `normalizePath()` 确保 URL 格式正确

现在头像图片可以正常显示了！🎉
