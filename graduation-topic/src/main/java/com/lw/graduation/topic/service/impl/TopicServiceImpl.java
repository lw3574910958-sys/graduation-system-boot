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
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.TopicStatus;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题目服务实现类
 * 实现题目管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TopicServiceImpl extends ServiceImpl<BizTopicMapper, BizTopic> implements TopicService {

    private final BizTopicMapper bizTopicMapper;
    private final CacheHelper cacheHelper;

    @Override
    public IPage<TopicVO> getTopicPage(TopicPageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getTitle() != null, BizTopic::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getTeacherId() != null, BizTopic::getTeacherId, queryDTO.getTeacherId())
                .eq(queryDTO.getStatus() != null, BizTopic::getStatus, queryDTO.getStatus())
                .eq(BizTopic::getIsDeleted, 0)
                .orderByDesc(BizTopic::getCreatedAt);

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

    @Override
    public TopicVO getTopicById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + id;
        
        return cacheHelper.getFromCache(cacheKey, TopicVO.class, () -> {
            BizTopic topic = bizTopicMapper.selectById(id);
            if (topic == null || topic.getIsDeleted() == 1) {
                return null;
            }
            return convertToTopicVO(topic);
        }, CacheConstants.ExpireTime.WARM_DATA_EXPIRE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTopic(TopicCreateDTO createDTO) {
        log.info("创建新题目: {}", createDTO.getTitle());
        
        // 1. 构造题目实体
        BizTopic topic = new BizTopic();
        topic.setTitle(createDTO.getTitle());
        topic.setDescription(createDTO.getDescription());
        // 注意：教师ID需要从上下文获取，这里暂时设置为0，实际应该从认证信息中获取
        topic.setTeacherId(0L);
        topic.setDepartmentId(createDTO.getDepartmentId());
        topic.setSource(createDTO.getSource());
        topic.setType(createDTO.getType());
        topic.setNature(createDTO.getNature());
        topic.setDifficulty(createDTO.getDifficulty());
        topic.setWorkload(createDTO.getWorkload());
        topic.setMaxSelections(createDTO.getMaxSelections() != null ? createDTO.getMaxSelections() : 1);
        topic.setSelectedCount(0);
        topic.setStatus(TopicStatus.OPEN.getValue()); // 默认开放状态
        
        // 2. 保存到数据库
        boolean saved = save(topic);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目创建失败");
        }
        
        // 3. 清除相关缓存
        clearTopicCache(topic.getId());
        
        log.info("题目创建成功，ID: {}", topic.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Long id, TopicUpdateDTO updateDTO) {
        log.info("更新题目: {}", id);
        
        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }
        
        // 2. 检查题目状态是否允许修改
        if (TopicStatus.getByValue(existingTopic.getStatus()) == TopicStatus.SELECTED) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已选题目不能修改");
        }
        
        // 3. 更新题目信息
        existingTopic.setTitle(updateDTO.getTitle());
        existingTopic.setDescription(updateDTO.getDescription());
        existingTopic.setSource(updateDTO.getSource());
        existingTopic.setType(updateDTO.getType());
        existingTopic.setNature(updateDTO.getNature());
        existingTopic.setDifficulty(updateDTO.getDifficulty());
        existingTopic.setWorkload(updateDTO.getWorkload());
        existingTopic.setMaxSelections(updateDTO.getMaxSelections());
        
        // 只有开放状态的题目才能改变状态
        if (TopicStatus.getByValue(existingTopic.getStatus()) == TopicStatus.OPEN 
            && updateDTO.getStatus() != null) {
            existingTopic.setStatus(updateDTO.getStatus());
        }
        
        // 4. 保存更新
        boolean updated = updateById(existingTopic);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目更新失败");
        }
        
        // 5. 清除缓存
        clearTopicCache(id);
        
        log.info("题目更新成功，ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long id) {
        log.info("删除题目: {}", id);
        
        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }
        
        // 2. 检查题目状态是否允许删除
        if (TopicStatus.getByValue(existingTopic.getStatus()) == TopicStatus.SELECTED) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已选题目不能删除");
        }
        
        // 3. 逻辑删除
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目删除失败");
        }
        
        // 4. 清除缓存
        clearTopicCache(id);
        
        log.info("题目删除成功，ID: {}", id);
    }

    /**
     * 教师获取自己发布的题目列表
     *
     * @param teacherId 教师ID
     * @param status 题目状态(null表示所有状态)
     * @return 题目列表
     */
    public List<TopicVO> getTopicsByTeacher(Long teacherId, Integer status) {
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getTeacherId, teacherId)
               .eq(BizTopic::getIsDeleted, 0);
        
        if (status != null) {
            wrapper.eq(BizTopic::getStatus, status);
        }
        
        wrapper.orderByDesc(BizTopic::getCreatedAt);
        
        return list(wrapper).stream()
                .map(this::convertToTopicVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取可选题目列表（开放状态的题目）
     *
     * @param departmentId 院系ID(null表示所有院系)
     * @return 可选题目列表
     */
    public List<TopicVO> getAvailableTopics(Long departmentId) {
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getStatus, TopicStatus.OPEN.getValue()) // 开放状态
               .apply("selected_count < max_selections") // 未满员
               .eq(BizTopic::getIsDeleted, 0);
        
        if (departmentId != null) {
            wrapper.eq(BizTopic::getDepartmentId, departmentId);
        }
        
        wrapper.orderByDesc(BizTopic::getCreatedAt);
        
        return list(wrapper).stream()
                .map(this::convertToTopicVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换题目实体为VO
     */
    private TopicVO convertToTopicVO(BizTopic topic) {
        return BeanMapperUtil.copyProperties(topic, TopicVO.class);
    }

    /**
     * 清除题目相关缓存
     */
    private void clearTopicCache(Long topicId) {
        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + topicId;
        cacheHelper.evictCache(cacheKey);
    }
}