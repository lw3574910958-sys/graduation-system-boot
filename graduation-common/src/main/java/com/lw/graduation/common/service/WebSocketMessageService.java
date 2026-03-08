package com.lw.graduation.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final ObjectMapper objectMapper;

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
     * @param targetScope   目标范围
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
        
        // 广播给所有在线用户
        broadcast("/topic/notice", message);
        
        log.info("已发送公告通知 - ID: {}, 标题：{}", noticeId, title);
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
