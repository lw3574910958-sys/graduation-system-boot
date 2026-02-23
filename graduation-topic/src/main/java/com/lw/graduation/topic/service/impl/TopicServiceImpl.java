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
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.topic.service.internal.TopicInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final BizSelectionMapper bizSelectionMapper;
    private final CacheHelper cacheHelper;
    private final TopicInternalService topicInternalService; // 注入内部服务

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
                .toList());
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
        TopicStatus currentStatus = TopicStatus.getByValue(existingTopic.getStatus());
        if (currentStatus != null && !currentStatus.isSelectable()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "当前状态的题目不能修改");
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
        TopicStatus currentStatus = TopicStatus.getByValue(existingTopic.getStatus());
        if (currentStatus != null && !currentStatus.isActive()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已关闭的题目不能删除");
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

    @Override
    public List<TopicVO> getSelectableTopics(Long departmentId) {
        log.info("获取可选题目列表，院系ID: {}", departmentId);

        // 1. 构建查询条件
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getIsDeleted, 0);

        // 如果指定了院系，则按院系筛选
        if (departmentId != null) {
            wrapper.eq(BizTopic::getDepartmentId, departmentId);
        }

        // 2. 查询所有题目
        List<BizTopic> allTopics = list(wrapper);

        // 3. 使用isSelectable()方法过滤可选题目
        List<BizTopic> selectableTopics = allTopics.stream()
                .filter(topic -> {
                    TopicStatus status = TopicStatus.getByValue(topic.getStatus());
                    return status != null && status.isSelectable();
                })
                .toList();

        // 4. 转换为VO
        return selectableTopics.stream()
                .map(this::convertToTopicVO)
                .toList();
    }
    
    /**
     * 获取可选题目列表（开放状态且未满员的题目）
     * 学生选题功能的核心方法
     * 
     * @param departmentId 院系ID(null表示所有院系)
     * @return 可选题目列表
     */
    public List<TopicVO> getAvailableTopics(Long departmentId) {
        log.info("获取可选题目列表（开放且未满员），院系ID: {}", departmentId);
        
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
                .toList();
    }

    /**
     * 教师获取自己发布的题目列表
     * 教师管理功能接口
     *
     * @param teacherId 教师ID
     * @param status 题目状态(null表示所有状态)
     * @return 题目列表
     */
    public List<TopicVO> getTopicsByTeacher(Long teacherId, Integer status) {
        log.info("教师[{}] 获取题目列表，状态: {}", teacherId, status);
        
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getTeacherId, teacherId)
               .eq(BizTopic::getIsDeleted, 0);

        if (status != null) {
            wrapper.eq(BizTopic::getStatus, status);
        }

        wrapper.orderByDesc(BizTopic::getCreatedAt);

        return list(wrapper).stream()
                .map(this::convertToTopicVO)
                .toList();
    }

    /**
     * 转换题目实体为VO
     */
    private TopicVO convertToTopicVO(BizTopic topic) {
        return BeanMapperUtil.copyProperties(topic, TopicVO.class);
    }

    /**
     * 更新题目状态
     * 已废弃：请使用TopicInternalService中的事务安全版本
     * 
     * @param topicId 题目ID
     * @param newStatus 新状态
     * @param validateTransition 是否验证状态转换合法性
     * @deprecated 使用 {@link TopicInternalService#updateTopicStatus(Long, Integer)} 替代
     */
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public void updateTopicStatus(Long topicId, TopicStatus newStatus, boolean validateTransition) {
        log.warn("调用了已废弃的updateTopicStatus方法，请使用TopicInternalService替代");
        
        log.info("更新题目 {} 状态为: {}", topicId, newStatus != null ? newStatus.getDescription() : "未知状态");

        BizTopic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        TopicStatus currentStatus = TopicStatus.getByValue(topic.getStatus());

        // 验证状态转换合法性
        if (validateTransition && newStatus != null && !isValidStatusTransition(currentStatus, newStatus)) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(),
                String.format("非法的状态转换: %s -> %s",
                    currentStatus != null ? currentStatus.getDescription() : "未知状态",
                    newStatus.getDescription()));
        }

        // 更新状态
        if (newStatus != null) {
            topic.setStatus(newStatus.getValue());
        }
        boolean updated = updateById(topic);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目状态更新失败");
        }

        // 清除缓存
        clearTopicCache(topicId);

        log.info("题目 {} 状态更新成功: {} -> {}", topicId,
                currentStatus != null ? currentStatus.getDescription() : "未知状态",
                newStatus != null ? newStatus.getDescription() : "未知状态");
    }

    /**
     * 检查题目状态转换是否合法
     *
     * @param current 当前状态
     * @param target 目标状态
     * @return 合法返回true
     */
    private boolean isValidStatusTransition(TopicStatus current, TopicStatus target) {
        if (current == null || target == null) {
            return false;
        }

        // 相同状态转换总是合法的
        if (current == target) {
            return true;
        }

        // 定义合法的状态转换规则
        return switch (current) {
            case OPEN ->
                // 开放状态可以转为审核中或关闭
                target == TopicStatus.REVIEWING || target == TopicStatus.CLOSED;
            case REVIEWING ->
                // 审核中状态可以转为开放、已选或关闭
                target == TopicStatus.OPEN || target == TopicStatus.SELECTED || target == TopicStatus.CLOSED;
            case SELECTED ->
                // 已选状态只能转为关闭
                target == TopicStatus.CLOSED;
            case CLOSED ->
                // 关闭状态不能转换
                false;
        };
    }

    /**
     * 处理选题申请事件，更新题目状态
     *
     * @param topicId 题目ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleSelectionApplied(Long topicId) {
        BizTopic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            return;
        }

        // 如果题目是开放状态，则转为审核中
        if (TopicStatus.getByValue(topic.getStatus()) == TopicStatus.OPEN) {
            // 通过内部服务更新状态，确保事务生效
            topicInternalService.updateTopicStatus(topicId, TopicStatus.REVIEWING.getValue());
            clearTopicCache(topicId); // 手动清除缓存
            log.info("题目[{}] 操作完成: 因收到选题申请转为审核中状态", topicId);
        }
    }

    /**
     * 处理选题审核结果事件
     * 
     * @param topicId 题目ID
     * @param selectionApproved 审核是否通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleSelectionReviewed(Long topicId, boolean selectionApproved) {
        BizTopic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            return;
        }
            
        TopicStatus currentStatus = TopicStatus.getByValue(topic.getStatus());
            
        // 如果题目当前是审核中状态
        if (currentStatus == TopicStatus.REVIEWING) {
            // 审核通过时检查所有待处理和已通过的申请
            // 审核驳回时只检查待审核的申请
            java.util.function.Predicate<BizSelection> filter = selectionApproved ? 
                selection -> selection.isPendingReview() || selection.isApproved() :
                BizSelection::isPendingReview;
                
            handleTopicStatusRecovery(topicId, filter);
        }
    }
        
    /**
     * 处理题目状态恢复逻辑
     * 
     * @param topicId 题目ID
     * @param selectionFilter 选题过滤条件
     */
    private void handleTopicStatusRecovery(Long topicId, java.util.function.Predicate<BizSelection> selectionFilter) {
        // 检查是否还有符合条件的申请
        LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSelection::getTopicId, topicId)
               .eq(BizSelection::getIsDeleted, 0);
            
        long pendingCount = bizSelectionMapper.selectList(wrapper).stream()
                .filter(selectionFilter)
                .count();
            
        // 如果没有待处理的申请，恢复为开放状态
        if (pendingCount == 0) {
            // 通过内部服务更新状态，确保事务生效
            topicInternalService.updateTopicStatus(topicId, TopicStatus.OPEN.getValue());
            clearTopicCache(topicId); // 手动清除缓存
            log.info("题目[{}] 操作完成: 所有申请处理完毕，恢复为开放状态", topicId);
        }
    }

    /**
     * 处理学生确认选题事件
     *
     * @param topicId 题目ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleSelectionConfirmed(Long topicId) {
        BizTopic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted() == 1) {
            return;
        }

        // 更新已选人数
        topicInternalService.updateSelectedCount(topicId, 1);
                
        // 更新状态为已选
        topicInternalService.updateTopicStatus(topicId, TopicStatus.SELECTED.getValue());
        clearTopicCache(topicId); // 手动清除缓存
                
        // 检查是否达到人数上限
        if (topic.getSelectedCount() >= topic.getMaxSelections()) {
            topicInternalService.updateTopicStatus(topicId, TopicStatus.CLOSED.getValue());
            clearTopicCache(topicId); // 手动清除缓存
            log.info("题目[{}] 操作完成: 达到选题人数上限，自动关闭", topicId);
        }

        log.info("题目[{}] 操作完成: 当前已选人数 {}/{}", topicId, topic.getSelectedCount(), topic.getMaxSelections());
    }

    /**
     * 清除题目相关缓存
     */
    private void clearTopicCache(Long topicId) {
        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + topicId;
        cacheHelper.evictCache(cacheKey);
    }
}