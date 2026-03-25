package com.lw.graduation.common.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 业务消息服务
 * 用于向客户端推送各类业务状态变更通知
 *
 * @author lw
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 用户类型提供者接口
     * 用于解耦，避免 common 模块直接依赖 infrastructure
     */
    @FunctionalInterface
    public interface UserTypeProvider {
        List<Long> getUserIdsByType(String userType);
    }
    
    private UserTypeProvider userTypeProvider;

    /**
     * 广播消息到 /topic（所有订阅者都会收到）
     */
    public void broadcast(String destination, Object message) {
        try {
            log.debug("广播消息到 {}: {}", destination, message);
            messagingTemplate.convertAndSend(destination, message);
        } catch (Exception e) {
            log.error("广播消息失败：{}", destination, e);
        }
    }

    /**
     * 发送消息给特定用户
     */
    public void sendToUser(String userId, String destination, Object message) {
        try {
            log.debug("发送消息给用户 {}: {}", userId, message);
            messagingTemplate.convertAndSendToUser(userId, destination, message);
        } catch (Exception e) {
            log.error("发送消息给用户 {} 失败：{}", userId, destination, e);
        }
    }

    /**
     * 发送公告通知
     *
     * @param noticeId      公告 ID
     * @param title         公告标题
     * @param type          公告类型
     * @param targetScope   目标范围 (0-全体，1-学生，2-教师，3-管理员)
     * @param publisherName 发布者姓名
     */
    public void sendNoticeNotification(Long noticeId, String title, Integer type, 
                                       Integer targetScope, String publisherName) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NOTICE_PUBLISHED");
        message.put("noticeId", noticeId);
        message.put("title", title);
        message.put("typeDesc", getTypeDescription(type));
        message.put("targetScope", getTargetScopeDescription(targetScope));
        message.put("publisherName", publisherName);
        message.put("timestamp", LocalDateTime.now().toString());
        
        // 根据目标范围决定推送给哪些用户
        if (targetScope == null || targetScope == 0) {
            // 全体：广播给所有用户
            broadcast("/topic/notice", message);
            log.info("已发送公告通知给全体用户 - ID: {}, 标题：{}", noticeId, title);
        } else if (targetScope == 1) {
            // 学生：推送给学生用户
            sendToUserType("/topic/notice", message, "student");
            log.info("已发送公告通知给学生用户 - ID: {}, 标题：{}", noticeId, title);
        } else if (targetScope == 2) {
            // 教师：推送给教师用户
            sendToUserType("/topic/notice", message, "teacher");
            log.info("已发送公告通知给教师用户 - ID: {}, 标题：{}", noticeId, title);
        } else if (targetScope == 3) {
            // 管理员：推送给管理员用户
            sendToUserType("/topic/notice", message, "admin");
            log.info("已发送公告通知给管理员用户 - ID: {}, 标题：{}", noticeId, title);
        }
    }

    /**
     * 设置用户类型提供者
     * 由上层模块（如 API 模块）提供具体实现
     * 
     * @param provider 用户类型提供者
     */
    public void setUserTypeProvider(UserTypeProvider provider) {
        this.userTypeProvider = provider;
    }
    
    /**
     * 根据用户类型推送消息
     * 需要获取该类型的所有在线用户并推送
     *
     * @param destination 目标地址
     * @param message 消息内容
     * @param userType 用户类型 (student, teacher, admin)
     */
    private void sendToUserType(String destination, Object message, String userType) {
        if (userTypeProvider == null) {
            log.warn("未设置 UserTypeProvider，无法按用户类型 {} 推送消息，降级为广播", userType);
            broadcast(destination, message);
            return;
        }
        
        try {
            // 1. 获取指定类型的所有用户 ID 列表
            List<Long> userIds = userTypeProvider.getUserIdsByType(userType);
            
            if (userIds.isEmpty()) {
                log.debug("未找到任何 {} 类型的用户", userType);
                return;
            }
            
            // 2. 遍历用户 ID 列表，逐个推送
            int successCount = 0;
            for (Long userId : userIds) {
                try {
                    // 检查用户是否在线（通过 StpUtil）
                    if (StpUtil.isLogin(userId)) {
                        messagingTemplate.convertAndSendToUser(
                            userId.toString(), 
                            destination, 
                            message
                        );
                        successCount++;
                    }
                } catch (Exception e) {
                    log.debug("推送消息给用户 {} 失败：{}", userId, e.getMessage());
                }
            }
            
            log.info("已向 {}/{} 个 {} 用户推送消息到 {}", 
                    successCount, userIds.size(), userType, destination);
        } catch (Exception e) {
            log.error("按用户类型推送消息失败：userType={}, destination={}", userType, destination, e);
        }
    }

    /**
     * 发送课题状态变更通知
     */
    public void sendTopicStatusChanged(Long topicId, String topicTitle, 
                                      Integer oldStatus, Integer newStatus) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TOPIC_STATUS_CHANGED");
        message.put("topicId", topicId);
        message.put("topicTitle", topicTitle);
        message.put("oldStatus", oldStatus);
        message.put("newStatus", newStatus);
        message.put("timestamp", LocalDateTime.now().toString());
        
        broadcast("/topic/topic", message);
        
        log.info("已发送课题状态变更通知 - ID: {}, 新状态：{}", topicId, newStatus);
    }

    /**
     * 发送文档审核结果通知
     */
    public void sendDocumentReviewed(Long documentId, String documentTitle, 
                                    Integer reviewStatus, String reviewerName) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "DOCUMENT_REVIEWED");
        message.put("documentId", documentId);
        message.put("documentTitle", documentTitle);
        message.put("reviewStatus", reviewStatus);
        message.put("reviewerName", reviewerName);
        message.put("timestamp", LocalDateTime.now().toString());
        
        broadcast("/topic/document", message);
        
        log.info("已发送文档审核结果通知 - ID: {}, 审核状态：{}", documentId, reviewStatus);
    }

    /**
     * 发送选题审核结果通知
     */
    public void sendSelectionApproved(Long selectionId, String topicTitle, 
                                     Integer approvalStatus, String reviewerName) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "SELECTION_APPROVED");
        message.put("selectionId", selectionId);
        message.put("topicTitle", topicTitle);
        message.put("approvalStatus", approvalStatus);
        message.put("reviewerName", reviewerName);
        message.put("timestamp", LocalDateTime.now().toString());
        
        broadcast("/topic/selection", message);
        
        log.info("已发送选题审核结果通知 - ID: {}, 审核状态：{}", selectionId, approvalStatus);
    }

    /**
     * 发送成绩更新通知
     */
    public void sendGradeUpdated(Long gradeId, String topicTitle, Double score) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "GRADE_UPDATED");
        message.put("gradeId", gradeId);
        message.put("topicTitle", topicTitle);
        message.put("score", score);
        message.put("timestamp", LocalDateTime.now().toString());
        
        broadcast("/topic/grade", message);
        
        log.info("已发送成绩更新通知 - ID: {}, 分数：{}", gradeId, score);
    }

    /**
     * 获取类型描述
     */
    private String getTypeDescription(Integer type) {
        return switch (type) {
            case 1 -> "系统通知";
            case 2 -> "公告";
            case 3 -> "提醒";
            default -> "通知";
        };
    }

    /**
     * 获取目标范围描述
     */
    private String getTargetScopeDescription(Integer targetScope) {
        return switch (targetScope) {
            case 0 -> "全体";
            case 1 -> "学生";
            case 2 -> "教师";
            case 3 -> "管理员";
            default -> "未知";
        };
    }
}
