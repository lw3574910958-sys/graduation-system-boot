# WebSocket连接错误诊断与解决方案

## 问题现象
WebSocket连接失败，错误信息：
```
WebSocket连接错误: Event {isTrusted: true, type: 'error', target: WebSocket, ...}
readyState: 3 (CLOSED)
url: 'ws://localhost:8080/api/ws/status?token=3f59f138-601d-467d-a8a3-63b5db980314'
```

## 根本原因分析

### 1. 后端缺少WebSocket支持 ✅ **已确认**
经过全面检查，发现以下关键问题：

**缺失的依赖**：
- 后端项目中没有引入 `spring-boot-starter-websocket` 依赖
- BOM文件中未定义WebSocket相关版本管理

**缺失的实现**：
- 没有任何WebSocket端点实现 (@ServerEndpoint注解的类)
- 没有WebSocket配置类
- 没有消息处理器或事件监听器

**架构现状**：
- 前端已实现完整的WebSocket客户端功能
- 后端完全没有对应的服务器端实现
- 这是一个典型的前后端功能不匹配问题

### 2. 错误详细分析
```
readyState: 3 // CLOSED状态，表示连接已关闭
errorCode: 1006 // 异常关闭，通常是由于服务器端点不存在
```

## 解决方案

### 方案一：移除WebSocket功能（推荐短期方案）

考虑到当前项目状态和实际需求，建议暂时移除WebSocket相关功能：

#### 1. 修改前端WebSocket服务
```typescript
// src/utils/webSocketService.ts
connect() {
  // 暂时禁用WebSocket连接
  console.log('WebSocket功能暂未启用')
  return
  
  // 原有逻辑...
}
```

#### 2. 移除相关依赖引用
```typescript
// 移除businessStatusNotifier中的WebSocket相关调用
// 或将其改为console.log输出
```

#### 3. 更新路由守卫
```typescript
// src/router/index.ts
// 移除WebSocket连接相关的代码
```

### 方案二：实现完整的WebSocket支持（长期方案）

如果确实需要实时通知功能，需要完整实现：

#### 1. 添加WebSocket依赖
```xml
<!-- graduation-bom/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
    <version>${spring-boot.version}</version>
</dependency>
```

#### 2. 创建WebSocket配置类
```java
// graduation-api/src/main/java/com/lw/graduation/api/config/WebSocketConfig.java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new StatusWebSocketHandler(), "/api/ws/status")
                .setAllowedOrigins("*");
    }
}
```

#### 3. 实现WebSocket处理器
```java
// graduation-api/src/main/java/com/lw/graduation/api/websocket/StatusWebSocketHandler.java
@Component
public class StatusWebSocketHandler extends TextWebSocketHandler {
    
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket连接建立: {}", session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发送的心跳等消息
        String payload = message.getPayload();
        if ("HEARTBEAT".equals(payload)) {
            session.sendMessage(new TextMessage("{\"type\":\"HEARTBEAT\"}"));
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket连接关闭: {}", session.getId());
    }
    
    // 广播消息方法
    public void broadcastMessage(String message) {
        TextMessage textMessage = new TextMessage(message);
        sessions.parallelStream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("发送WebSocket消息失败", e);
                    }
                });
    }
}
```

#### 4. 创建业务消息服务
```java
// graduation-common/src/main/java/com/lw/graduation/common/service/BusinessMessageService.java
@Service
@RequiredArgsConstructor
public class BusinessMessageService {
    
    private final StatusWebSocketHandler webSocketHandler;
    
    /**
     * 发送课题状态变更通知
     */
    public void sendTopicStatusChanged(Long topicId, String topicTitle, 
                                     Integer oldStatus, Integer newStatus) {
        Map<String, Object> message = Map.of(
            "type", "TOPIC_STATUS_CHANGED",
            "topicId", topicId,
            "topicTitle", topicTitle,
            "oldStatus", oldStatus,
            "newStatus", newStatus,
            "timestamp", System.currentTimeMillis()
        );
        
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            webSocketHandler.broadcastMessage(jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("序列化WebSocket消息失败", e);
        }
    }
    
    // 其他业务消息发送方法...
}
```

#### 5. 在业务服务中集成消息发送
```java
// 在选题审核、文档审核等业务完成后调用
@Autowired
private BusinessMessageService businessMessageService;

// 选题审核完成后
businessMessageService.sendSelectionApproved(
    selection.getId(), 
    selection.getTopic().getTitle(),
    reviewResult
);
```

## 推荐处理方式

### 短期处理（立即执行）
1. **禁用WebSocket连接** - 修改前端代码暂时关闭WebSocket功能
2. **移除相关提示** - 避免用户看到连接失败的提示信息
3. **保持现有轮询机制** - 依赖HTTP请求进行状态更新

### 长期规划
1. **评估实际需求** - 确定是否真的需要实时通知功能
2. **制定实施计划** - 如果需要，则按方案二逐步实现
3. **性能测试** - 确保WebSocket实现不会影响系统性能

## 风险评估

### 不处理的风险
- 用户会持续看到连接错误信息
- 控制台会有大量错误日志
- 可能影响用户体验

### 实施方案一的风险
- 失去实时通知能力
- 需要依赖轮询或其他机制
- 但实现简单，风险最小

### 实施方案二的风险
- 实现复杂度高
- 需要考虑连接管理、心跳机制、异常处理
- 可能引入新的bug
- 需要额外的测试和维护成本

## 建议

**推荐采用方案一（短期处理）**，理由如下：
1. 当前系统功能完整，WebSocket并非核心功能
2. 实现成本低，风险小
3. 可以快速解决问题，避免用户困扰
4. 为后续可能的实时功能需求保留扩展空间

如果后续确实需要实时通知功能，再按方案二进行完整实现。