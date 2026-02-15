package com.lw.graduation.topic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.topic.TopicPageQueryDTO;
import com.lw.graduation.api.dto.topic.TopicUpdateDTO;
import com.lw.graduation.api.service.topic.TopicService;
import com.lw.graduation.api.vo.topic.TopicVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课题服务实现类
 * 实现课题管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicServiceImpl extends ServiceImpl<BizTopicMapper, BizTopic> implements TopicService {

    private final BizTopicMapper bizTopicMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询课题列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<TopicVO> getTopicPage(TopicPageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getTitle() != null, BizTopic::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getTeacherId() != null, BizTopic::getTeacherId, queryDTO.getTeacherId())
                .eq(queryDTO.getStatus() != null, BizTopic::getStatus, queryDTO.getStatus())
                .orderByDesc(BizTopic::getCreatedAt); // 按创建时间倒序

        // 2. 执行分页查询
        IPage<BizTopic> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizTopic> topicPage = bizTopicMapper.selectPage(page, wrapper);

        // 3. 转换为VO
        IPage<TopicVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(topicPage.getRecords().stream()
                .map(this::convertToTopicVO)
                .collect(Collectors.toList()));
        voPage.setTotal(topicPage.getTotal());

        return voPage;
    }

    /**
     * 根据ID获取课题详情（带缓存穿透防护）
     *
     * @param id 课题ID
     * @return 课题详情
     */
    @Override
    public TopicVO getTopicById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + id;
        
        // 1. 查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (CacheConstants.CacheValue.NULL_MARKER.equals(cached)) {
                log.debug("缓存命中空值标记，课题不存在: " + id);
                return null;
            }
            return (TopicVO) cached;
        }

        // 2. 缓存未命中，查数据库
        BizTopic topic = bizTopicMapper.selectById(id);
        if (topic == null) {
            // 缓存空值防止穿透
            redisTemplate.opsForValue().set(
                cacheKey,
                CacheConstants.CacheValue.NULL_MARKER,
                CacheConstants.CacheValue.NULL_EXPIRE,
                TimeUnit.SECONDS
            );
            log.debug("课题不存在，缓存空值标记: " + cacheKey);
            return null;
        }

        // 3. 转换并缓存结果
        TopicVO result = convertToTopicVO(topic);
        redisTemplate.opsForValue().set(
            cacheKey,
            result,
            CacheConstants.ExpireTime.TOPIC_INFO_EXPIRE,
            TimeUnit.SECONDS
        );
        log.debug("缓存课题信息: " + cacheKey);
        return result;
    }

    /**
     * 创建课题
     *
     * @param createDTO 创建参数
     */
    @Override
    @Transactional
    public void createTopic(TopicCreateDTO createDTO) {
        // 1. 创建课题实体
        BizTopic topic = new BizTopic();
        topic.setTitle(createDTO.getTitle());
        topic.setDescription(createDTO.getDescription());
        topic.setTeacherId(createDTO.getTeacherId());
        topic.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 0); // 默认开放

        // 2. 插入数据库
        bizTopicMapper.insert(topic);
        
        // 3. 清除相关缓存（如果有教师相关的缓存）
    }

    /**
     * 更新课题
     *
     * @param id 课题ID
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional
    public void updateTopic(Long id, TopicUpdateDTO updateDTO) {
        // 1. 查询课题是否存在
        BizTopic existingTopic = bizTopicMapper.selectById(id);
        if (existingTopic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 构建更新实体
        BizTopic updateTopic = new BizTopic();
        updateTopic.setId(id);
        updateTopic.setTitle(updateDTO.getTitle());
        updateTopic.setDescription(updateDTO.getDescription());
        if (updateDTO.getTeacherId() != null) {
            updateTopic.setTeacherId(updateDTO.getTeacherId());
        }
        if (updateDTO.getStatus() != null) {
            updateTopic.setStatus(updateDTO.getStatus());
        }
        updateTopic.setUpdatedAt(LocalDateTime.now());

        // 3. 执行更新
        bizTopicMapper.updateById(updateTopic);
        
        // 4. 清除缓存
        clearTopicCache(id);
    }

    /**
     * 删除课题
     *
     * @param id 课题ID
     */
    @Override
    @Transactional
    public void deleteTopic(Long id) {
        // 1. 检查课题是否存在
        BizTopic topic = bizTopicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 检查是否已被选中（状态为已选时不能删除）
        if (topic.getStatus() != null && topic.getStatus() == 1) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已选中的课题不能删除");
        }

        // 3. 执行删除（逻辑删除）
        bizTopicMapper.deleteById(id);
        
        // 4. 清除缓存
        clearTopicCache(id);
    }

    /**
     * 将BizTopic实体转换为TopicVO
     *
     * @param topic 课题实体
     * @return 课题VO
     */
    private TopicVO convertToTopicVO(BizTopic topic) {
        TopicVO vo = new TopicVO();
        vo.setId(topic.getId());
        vo.setTitle(topic.getTitle());
        vo.setDescription(topic.getDescription());
        vo.setTeacherId(topic.getTeacherId());
        vo.setStatus(topic.getStatus());
        vo.setCreatedAt(topic.getCreatedAt());
        vo.setUpdatedAt(topic.getUpdatedAt());
        return vo;
    }

    /**
     * 清除单个课题缓存
     */
    private void clearTopicCache(Long topicId) {
        if (topicId != null) {
            String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + topicId;
            redisTemplate.delete(cacheKey);
            log.debug("清除课题缓存: " + cacheKey);
        }
    }
}