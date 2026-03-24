package com.lw.graduation.topic.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.topic.TopicPageQueryDTO;
import com.lw.graduation.api.dto.topic.TopicReviewDTO;
import com.lw.graduation.api.dto.topic.TopicUpdateDTO;
import com.lw.graduation.api.service.topic.TopicService;
import com.lw.graduation.api.vo.topic.TopicVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.IEnum;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.topic.service.internal.TopicInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final BizTeacherMapper bizTeacherMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final CacheHelper cacheHelper;
    private final TopicInternalService topicInternalService; // 注入内部服务

    /**
     * 获取题目分页列表
     *
     * @param queryDTO 查询参数
     * @return 分页列表
     */
    @Override
    public IPage<TopicVO> getTopicPage(TopicPageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getTitle() != null, BizTopic::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getTeacherId() != null, BizTopic::getTeacherId, queryDTO.getTeacherId())
                .eq(queryDTO.getStatus() != null, BizTopic::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getSource() != null, BizTopic::getSource, queryDTO.getSource())
                .eq(queryDTO.getType() != null, BizTopic::getType, queryDTO.getType())
                .eq(queryDTO.getNature() != null, BizTopic::getNature, queryDTO.getNature())
                .eq(queryDTO.getDifficulty() != null, BizTopic::getDifficulty, queryDTO.getDifficulty())
                .eq(queryDTO.getWorkload() != null, BizTopic::getWorkload, queryDTO.getWorkload())
                .eq(BizTopic::getIsDeleted, 0)
                .orderByDesc(BizTopic::getCreatedAt);

        // 2. 执行分页查询
        IPage<BizTopic> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizTopic> topicPage = bizTopicMapper.selectPage(page, wrapper);

        // 3. 转换为 VO
        IPage<TopicVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(topicPage.getRecords().stream()
                .map(this::convertToTopicVO)
                .toList());
        voPage.setTotal(topicPage.getTotal());

        return voPage;
    }

    /**
     * 获取题目详情
     *
     * @param id 题目 ID
     * @return 详情
     */
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

    /**
     * 创建新题目
     *
     * @param createDTO 创建参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTopic(TopicCreateDTO createDTO) {
        log.info("创建新题目：{}", createDTO.getTitle());

        // 1. 获取当前登录教师信息
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("当前登录用户 ID: {}", currentUserId);

        // 查询教师信息
        LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(BizTeacher::getUserId, currentUserId);
        BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);

        if (teacher == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "未找到教师信息");
        }

        log.info("教师信息：ID={}, 工号={}, 院系 ID={}", teacher.getId(), teacher.getTeacherId(), teacher.getDepartmentId());

        // 2. 构造题目实体
        BizTopic topic = new BizTopic();
        topic.setTitle(createDTO.getTitle());
        topic.setDescription(createDTO.getDescription());
        topic.setTeacherId(teacher.getId()); // 使用教师表的 ID
        topic.setDepartmentId(teacher.getDepartmentId()); // 从教师信息中获取院系 ID
        topic.setSource(createDTO.getSource());
        topic.setType(createDTO.getType());
        topic.setNature(createDTO.getNature());
        topic.setDifficulty(createDTO.getDifficulty());
        topic.setWorkload(createDTO.getWorkload());
        topic.setMaxSelections(createDTO.getMaxSelections() != null ? createDTO.getMaxSelections() : 1);
        topic.setSelectedCount(0); // 新增时已选人数为 0
        topic.setStatus(TopicStatus.DRAFT.getCode()); // 默认草稿状态，需要提交审核

        // 3. 保存到数据库
        boolean saved = save(topic);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目创建失败");
        }

        // 4. 清除相关缓存
        clearTopicCache(topic.getId());

        log.info("题目创建成功，ID: {}", topic.getId());
    }

    /**
     * 更新题目信息
     *
     * @param id      题目 ID
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTopic(Long id, TopicUpdateDTO updateDTO) {
        log.info("更新题目：{}", id);

        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 只有草稿状态才能修改
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.DRAFT) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的题目才能修改");
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

        // 4. 保存更新
        boolean updated = updateById(existingTopic);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目更新失败");
        }

        // 5. 清除缓存
        clearTopicCache(id);

        log.info("题目更新成功，ID: {}", id);
    }

    /**
     * 删除题目
     *
     * @param id 题目 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long id) {
        log.info("删除题目：{}", id);

        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 只有草稿状态才能删除（撤销）
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.DRAFT) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的题目才能撤销");
        }

        // 3. 逻辑删除
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目撤销失败");
        }

        // 4. 清除缓存
        clearTopicCache(id);

        log.info("题目撤销成功，ID: {}", id);
    }

    /**
     * 提交题目审核
     *
     * @param topicId 待审核的题目 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long topicId) {
        log.info("教师提交题目审核：{}", topicId);

        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(topicId);
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 检查题目状态是否为草稿（只有草稿才能提交审核）
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.DRAFT) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的题目才能提交审核");
        }

        // 3. 更新状态为审核中
        topicInternalService.updateTopicStatus(topicId, TopicStatus.REVIEWING.getCode());
        clearTopicCache(topicId);

        log.info("题目 [{}] 提交审核成功，当前状态：审核中", topicId);
    }

    /**
     * 审核题目
     *
     * @param reviewDTO 审核信息
     * @param reviewerId 审核人 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewTopic(TopicReviewDTO reviewDTO, Long reviewerId) {
        log.info("审核题目：{}, 审核人：{}, 审核结果：{}", reviewDTO.getTopicId(), reviewerId, reviewDTO.getReviewResult());

        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(reviewDTO.getTopicId());
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 检查题目状态是否为审核中
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.REVIEWING) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "当前题目不在审核状态");
        }

        // 3. 根据审核结果更新状态和审核结果字段
        if (reviewDTO.getReviewResult() == 1) {
            // 审核通过：转为开放状态，清除驳回记录
            topicInternalService.updateTopicStatus(reviewDTO.getTopicId(), TopicStatus.OPEN.getCode());
            // 更新最近一次审核结果为通过
            updateLastReviewOutcome(reviewDTO.getTopicId(), 1, null);
            log.info("题目 [{}] 审核通过，转为开放状态", reviewDTO.getTopicId());
        } else if (reviewDTO.getReviewResult() == 2) {
            // 审核驳回：退回草稿状态，记录驳回意见
            topicInternalService.updateTopicStatus(reviewDTO.getTopicId(), TopicStatus.DRAFT.getCode());
            // 更新最近一次审核结果为驳回，并保存审核意见
            updateLastReviewOutcome(reviewDTO.getTopicId(), 2, reviewDTO.getReviewComment());
            log.info("题目 [{}] 审核驳回，审核意见：{}", reviewDTO.getTopicId(), reviewDTO.getReviewComment());
        } else {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "无效的审核结果");
        }

        // 4. 清除缓存
        clearTopicCache(reviewDTO.getTopicId());
    }

    /**
     * 更新最近一次审核结果（包含反馈意见）
     *
     * @param topicId 题目 ID
     * @param reviewOutcome 审核结果：1-通过，2-驳回
     * @param feedback 审核意见
     */
    private void updateLastReviewOutcome(Long topicId, Integer reviewOutcome, String feedback) {
        LambdaUpdateWrapper<BizTopic> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BizTopic::getId, topicId)
               .set(BizTopic::getLastReviewOutcome, reviewOutcome)
               // 审核通过时清除驳回意见，驳回时保留意见
               .set(BizTopic::getLastReviewFeedback, reviewOutcome == 1 ? null : feedback)
               .set(BizTopic::getReviewerId, StpUtil.getLoginIdAsLong())
               .set(BizTopic::getReviewedAt, LocalDateTime.now());
        update(wrapper);
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
        wrapper.eq(BizTopic::getStatus, TopicStatus.OPEN.getCode()) // 开放状态
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
     * 转换为 TopicVO（包含教师工号和院系名称）
     */
    private TopicVO convertToTopicVO(BizTopic topic) {
        TopicVO vo = BeanMapperUtil.copyProperties(topic, TopicVO.class);

        // 填充教师工号
        if (topic.getTeacherId() != null) {
            LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
            teacherWrapper.eq(BizTeacher::getId, topic.getTeacherId());
            BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
            if (teacher != null) {
                vo.setTeacherNumber(teacher.getTeacherId());

                // 填充院系名称（从教师表中获取）
                if (teacher.getDepartmentId() != null) {
                    SysDepartment department = sysDepartmentMapper.selectById(teacher.getDepartmentId());
                    if (department != null) {
                        vo.setDepartmentName(department.getName());
                    }
                }
            }
        }

        return vo;
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
        if (IEnum.getByCode(TopicStatus.class,topic.getStatus()) == TopicStatus.OPEN) {
            // 通过内部服务更新状态，确保事务生效
            topicInternalService.updateTopicStatus(topicId, TopicStatus.REVIEWING.getCode());
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

        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class,topic.getStatus());

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
            topicInternalService.updateTopicStatus(topicId, TopicStatus.OPEN.getCode());
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
        clearTopicCache(topicId); // 手动清除缓存

        // 检查是否达到人数上限，若达到则自动关闭
        topic = getById(topicId); // 重新获取最新数据
        if (topic.getSelectedCount() >= topic.getMaxSelections()) {
            topicInternalService.updateTopicStatus(topicId, TopicStatus.CLOSED.getCode());
            clearTopicCache(topicId); // 手动清除缓存
            log.info("题目 [{}] 操作完成：达到选题人数上限，自动关闭", topicId);
        } else {
            log.info("题目 [{}] 操作完成：当前已选人数 {}/{}", topicId, topic.getSelectedCount(), topic.getMaxSelections());
        }
    }

    /**
     * 开放课题
     *
     * @param id 课题 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openTopic(Long id) {
        log.info("开放课题：{}", id);
        toggleTopicStatusInternal(id, TopicStatus.OPEN.getCode(), "开放");
    }
    
    /**
     * 关闭课题
     *
     * @param id 课题 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeTopic(Long id) {
        log.info("关闭课题：{}", id);
        toggleTopicStatusInternal(id, TopicStatus.CLOSED.getCode(), "关闭");
    }

    /**
     * 内部方法：切换课题状态
     *
     * @param id 课题 ID
     * @param newStatus 新状态
     * @param action 操作描述
     */
    private void toggleTopicStatusInternal(Long id, Integer newStatus, String action) {
        // 1. 检查课题是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "课题不存在");
        }

        // 2. 检查当前状态
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "当前课题状态异常");
        }

        // 3. 如果已经是目标状态，直接返回
        if (existingTopic.getStatus().equals(newStatus)) {
            log.info("课题状态已是目标状态：ID={}, 状态={}", id, action);
            return;
        }

        // 4. 状态转换检查
        if (newStatus == 2) { // 开放
            if (existingTopic.getSelectedCount() >= existingTopic.getMaxSelections()) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "课题已达人数上限，无法开放");
            }
        }

        // 5. 更新状态
        topicInternalService.updateTopicStatus(id, newStatus);
        clearTopicCache(id);

        log.info("课题状态{}成功：ID={}", action, id);
    }
    private void clearTopicCache(Long topicId) {
        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + topicId;
        cacheHelper.evictCache(cacheKey);
    }
}