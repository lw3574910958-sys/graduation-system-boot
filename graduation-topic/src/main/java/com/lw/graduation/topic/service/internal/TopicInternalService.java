package com.lw.graduation.topic.service.internal;

import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 题目内部服务类
 * 专门处理需要事务保护的核心数据库操作
 * 避免@Transactional自调用问题
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicInternalService {

    private final BizTopicMapper bizTopicMapper;

    /**
     * 更新题目状态（带事务保护）
     * 
     * @param topicId 题目ID
     * @param newStatusValue 新状态值
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTopicStatus(Long topicId, Integer newStatusValue) {
        log.debug("开始更新题目 {} 状态为: {}", topicId, newStatusValue);
        
        BizTopic topic = bizTopicMapper.selectById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            logTopicNotFound(topicId);
            return;
        }
        
        // 更新状态
        topic.setStatus(newStatusValue);
        boolean updated = bizTopicMapper.updateById(topic) > 0;
        
        if (updated) {
            TopicStatus newStatus = TopicStatus.getByValue(newStatusValue);
            String currentStatusDesc = getCurrentStatusDescription(topic);
            String newStatusDesc = newStatus != null ? newStatus.getDescription() : "未知状态";
            log.info("题目[{}] 状态变更: {} -> {}", topicId, currentStatusDesc, newStatusDesc);
        } else {
            log.error("题目[{}] 状态更新失败", topicId);
        }
    }
    
    /**
     * 更新题目已选人数（带事务保护）
     * 
     * @param topicId 题目ID
     * @param increment 增量（通常为1）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSelectedCount(Long topicId, int increment) {
        log.debug("更新题目 {} 已选人数，增量: {}", topicId, increment);
        
        BizTopic topic = bizTopicMapper.selectById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            logTopicNotFound(topicId);
            return;
        }
        
        // 更新已选人数
        int newCount = topic.getSelectedCount() + increment;
        topic.setSelectedCount(Math.max(0, newCount)); // 确保不为负数
        
        boolean updated = bizTopicMapper.updateById(topic) > 0;
        
        if (updated) {
            log.info("题目[{}] 选题人数更新: {} -> {}", 
                    topicId, topic.getSelectedCount() - increment, topic.getSelectedCount());
        } else {
            log.error("题目[{}] 选题人数更新失败", topicId);
        }
    }
    
    /**
     * 记录题目不存在警告日志
     * 
     * @param topicId 题目ID
     */
    private void logTopicNotFound(Long topicId) {
        log.warn("题目[{}] 不存在或已删除", topicId);
    }
    
    /**
     * 获取当前状态描述
     * 
     * @param topic 题目实体
     * @return 状态描述
     */
    private String getCurrentStatusDescription(BizTopic topic) {
        TopicStatus currentStatus = TopicStatus.getByValue(topic.getStatus());
        return currentStatus != null ? currentStatus.getDescription() : "未知状态";
    }
}