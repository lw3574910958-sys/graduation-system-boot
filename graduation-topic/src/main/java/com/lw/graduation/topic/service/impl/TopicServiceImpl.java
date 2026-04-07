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
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.domain.enums.status.ReviewStatus;
import com.lw.graduation.domain.enums.status.TopicReviewFilter;
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
    private final SysUserMapper sysUserMapper;
    private final DataPermissionUtil dataPermissionUtil; // 注入数据权限工具
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
                .eq(BizTopic::getIsDeleted, IsDelete.NOT_DELETED.getCode())
                .orderByDesc(BizTopic::getCreatedAt);

        // 2. 处理审核状态筛选
        applyReviewStatusFilter(wrapper, queryDTO);

        // 3. 根据用户类型添加权限过滤（使用通用方法）
        addPermissionFilter(wrapper);

        // 4. 执行分页查询
        IPage<BizTopic> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizTopic> topicPage = bizTopicMapper.selectPage(page, wrapper);

        // 5. 转换为 VO
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
            if (topic == null || topic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
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

        // 查询教师信息（复用 DataPermissionUtil 工具方法）
        Long teacherBizId = dataPermissionUtil.getTeacherIdByUserId(currentUserId);
        BizTeacher teacher = bizTeacherMapper.selectById(teacherBizId);

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
        
        // 根据传入的 status 字段设置初始状态
        // 如果 status=REVIEWING（直接提交审核），则设置为 REVIEWING；否则默认为 DRAFT
        if (createDTO.getStatus() != null && createDTO.getStatus().equals(TopicStatus.REVIEWING.getCode())) {
            topic.setStatus(TopicStatus.REVIEWING.getCode());
            log.info("题目创建时直接提交审核，ID: {}", topic.getId());
        } else {
            topic.setStatus(TopicStatus.DRAFT.getCode());
            log.info("题目创建为草稿状态，ID: {}", topic.getId());
        }

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
        if (existingTopic == null || existingTopic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
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
     * 撤销题目（仅草稿状态）
     *
     * @param id 题目 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeTopic(Long id) {
        log.info("撤销题目：{}", id);

        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 只有草稿状态才能撤销
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
     * 删除题目（仅审核通过状态）
     *
     * @param id 题目 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long id) {
        log.info("删除题目：{}", id);

        // 1. 检查题目是否存在
        BizTopic existingTopic = getById(id);
        if (existingTopic == null || existingTopic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 只有审核通过状态才能删除（开放或关闭）
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.OPEN && currentStatus != TopicStatus.CLOSED) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只能删除审核通过的题目（开放或关闭状态）");
        }

        // 3. 检查是否有学生已选该题目
        if (existingTopic.getSelectedCount() > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已有学生选题，无法删除");
        }

        // 4. 逻辑删除
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "题目删除失败");
        }

        // 5. 清除缓存
        clearTopicCache(id);

        log.info("题目删除成功，ID: {}", id);
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
        if (existingTopic == null || existingTopic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 检查题目状态是否为草稿（只有草稿才能提交审核）
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.DRAFT) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的题目才能提交审核");
        }

        // 3. 更新状态为审核中，并清除上一次的审核结果
        topicInternalService.updateTopicStatus(topicId, TopicStatus.REVIEWING.getCode());
        
        // 清除上一次的审核记录，表示这是一次新的审核申请
        LambdaUpdateWrapper<BizTopic> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BizTopic::getId, topicId)
               .set(BizTopic::getLastReviewOutcome, null)  // 清除审核结果
               .set(BizTopic::getLastReviewFeedback, null)  // 清除审核意见
               .set(BizTopic::getReviewerId, null)  // 清除审核人
               .set(BizTopic::getReviewedAt, null);  // 清除审核时间
        update(wrapper);
        
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
        if (existingTopic == null || existingTopic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 2. 检查题目状态是否为审核中
        TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, existingTopic.getStatus());
        if (currentStatus != TopicStatus.REVIEWING) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "当前题目不在审核状态");
        }

        // 3. 根据审核结果更新状态和审核结果字段
        if (reviewDTO.getReviewResult().equals(ReviewStatus.APPROVED.getCode())) {
            // 审核通过：转为开放状态
            topicInternalService.updateTopicStatus(reviewDTO.getTopicId(), TopicStatus.OPEN.getCode());
            // 更新最近一次审核结果为通过，并保存审核意见
            updateLastReviewOutcome(reviewDTO.getTopicId(), ReviewStatus.APPROVED.getCode(), reviewDTO.getReviewComment());
            log.info("题目 [{}] 审核通过，审核意见：{}", reviewDTO.getTopicId(), reviewDTO.getReviewComment());
        } else if (reviewDTO.getReviewResult().equals(ReviewStatus.REJECTED.getCode())) {
            // 审核驳回：退回草稿状态，记录驳回意见
            topicInternalService.updateTopicStatus(reviewDTO.getTopicId(), TopicStatus.DRAFT.getCode());
            // 更新最近一次审核结果为驳回，并保存审核意见
            updateLastReviewOutcome(reviewDTO.getTopicId(), ReviewStatus.REJECTED.getCode(), reviewDTO.getReviewComment());
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
               // 无论通过还是驳回，都保存审核意见
               .set(BizTopic::getLastReviewFeedback, feedback)
               .set(BizTopic::getReviewerId, StpUtil.getLoginIdAsLong())
               .set(BizTopic::getReviewedAt, LocalDateTime.now());
        update(wrapper);
    }

    /**
     * 转换为 TopicVO（包含教师工号和院系名称）
     */
    private TopicVO convertToTopicVO(BizTopic topic) {
        TopicVO vo = BeanMapperUtil.copyProperties(topic, TopicVO.class);

        // 填充教师工号和姓名
        if (topic.getTeacherId() != null) {
            LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
            teacherWrapper.eq(BizTeacher::getId, topic.getTeacherId());
            BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
            if (teacher != null) {
                vo.setTeacherNumber(teacher.getTeacherId());

                // 通过 userId 查询教师真实姓名
                if (teacher.getUserId() != null) {
                    SysUser user = sysUserMapper.selectById(teacher.getUserId());
                    if (user != null) {
                        vo.setTeacherName(user.getRealName());
                    }
                }

                // 填充院系名称和编码（从教师表中获取）
                if (teacher.getDepartmentId() != null) {
                    SysDepartment department = sysDepartmentMapper.selectById(teacher.getDepartmentId());
                    if (department != null) {
                        vo.setDepartmentName(department.getName());
                        vo.setDepartmentCode(department.getCode());
                    }
                }
            }
        }

        // 填充审核人姓名
        if (topic.getReviewerId() != null) {
            SysUser reviewer = sysUserMapper.selectById(topic.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getRealName());
            }
        }

        return vo;
    }
    
    /**
     * 处理选题申请事件（仅记录日志，不再更新题目状态）
     * 注意：学生选题时只更新选题表状态，不更新题目表状态
     * 题目状态应保持为 OPEN（开放），直到所有申请处理完毕或达到人数上限
     *
     * @param topicId 题目 ID
     */
    public void handleSelectionApplied(Long topicId) {
        BizTopic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            return;
        }
    
        // 不再更新题目状态，只记录日志
        log.info("收到题目 [{}] 的选题申请，题目状态保持为：{}", topicId, topic.getStatus());
    }

    /**
     * 处理选题审核结果事件（仅记录日志，不再更新题目状态）
     * 注意：选题审核只更新选题表状态，不影响题目表的状态
     * 题目状态由教师手动控制（开放/关闭），或达到人数上限时自动关闭
     *
     * @param topicId 题目 ID
     * @param selectionApproved 审核是否通过
     */
    public void handleSelectionReviewed(Long topicId, boolean selectionApproved) {
        // 不再更新题目状态，只记录日志
        log.info("题目 [{}] 的选题申请已审核（通过：{}），题目状态保持为：{}", 
                topicId, selectionApproved, getById(topicId).getStatus());
    }


    /**
     * 处理学生确认选题事件
     *
     * @param topicId 题目 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleSelectionConfirmed(Long topicId) {
        BizTopic topic = getById(topicId);
        if (topic == null || topic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            return;
        }
    
        // 确认选题时不再增加已选人数（已在申请时预占名额）
        // 只检查是否达到人数上限，若达到则自动关闭
        if (topic.getSelectedCount() >= topic.getMaxSelections()) {
            topicInternalService.updateTopicStatus(topicId, TopicStatus.CLOSED.getCode());
            clearTopicCache(topicId); // 手动清除缓存
            log.info("题目 [{}] 操作完成：达到选题人数上限，自动关闭", topicId);
        } else {
            log.info("题目 [{}] 学生确认选题，当前已选人数 {}/{}", topicId, topic.getSelectedCount(), topic.getMaxSelections());
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
        if (existingTopic == null || existingTopic.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
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
        if (newStatus.equals(TopicStatus.OPEN.getCode())) { // 开放
            if (existingTopic.getSelectedCount() >= existingTopic.getMaxSelections()) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "课题已达人数上限，无法开放");
            }
        }

        // 5. 更新状态
        topicInternalService.updateTopicStatus(id, newStatus);
        clearTopicCache(id);

        log.info("课题状态{}成功：ID={}", action, id);
    }

    /**
     * 应用审核状态筛选
     *
     * @param wrapper 查询条件
     * @param queryDTO 查询参数
     */
    private void applyReviewStatusFilter(LambdaQueryWrapper<BizTopic> wrapper, TopicPageQueryDTO queryDTO) {
        if (queryDTO.getReviewStatus() == null || queryDTO.getReviewStatus().trim().isEmpty()) {
            return; // 没有指定审核状态，不筛选
        }

        String reviewStatus = queryDTO.getReviewStatus();

        // 教师视角：1-审核通过，2-审核驳回
        // 管理员视角：pending-待审核，reviewed-已审核
        TopicReviewFilter filter = IEnum.getByCode(TopicReviewFilter.class, reviewStatus);
        if (filter == null) {
            return; // 无效的筛选条件，不处理
        }

        switch (filter) {
            case TEACHER_APPROVED -> { // 审核通过：status 为 OPEN(2) 或 CLOSED(3)
                wrapper.in(BizTopic::getStatus, TopicStatus.OPEN.getCode(), TopicStatus.CLOSED.getCode());
                log.info("教师查询审核通过的题目");
            }
            case TEACHER_REJECTED -> { // 审核驳回：status 为 DRAFT(0) 且 last_review_outcome 为 REJECTED(2)
                wrapper.eq(BizTopic::getStatus, TopicStatus.DRAFT.getCode())
                       .eq(BizTopic::getLastReviewOutcome, ReviewStatus.REJECTED.getCode());
                log.info("教师查询审核驳回的题目");
            }
            case ADMIN_PENDING -> { // 待审核：last_review_outcome 为 NULL
                wrapper.isNull(BizTopic::getLastReviewOutcome);
                log.info("管理员查询待审核的题目");
            }
            case ADMIN_REVIEWED -> { // 已审核：last_review_outcome 不为 NULL（包括通过和驳回）
                wrapper.isNotNull(BizTopic::getLastReviewOutcome);
                log.info("管理员查询已审核的题目");
            }
            default -> {} // 其他值不处理
        }
    }

    /**
     * 根据用户类型添加权限过滤条件（使用通用方法）
     * - 系统管理员：查看所有题目
     * - 院系管理员：只查看本院系的题目（排除草稿状态）
     * - 教师：只查看自己的题目
     * - 学生：
     *   - 未申请选题：只查看本院系教师开放的题目
     *   - 已申请选题：只查看已申请的题目
     *
     * @param wrapper 查询条件
     */
    private void addPermissionFilter(LambdaQueryWrapper<BizTopic> wrapper) {
        // 使用通用数据权限过滤方法
        dataPermissionUtil.addCommonDataPermissionFilter(
            // 学生：只查看本院系教师开放的题目 OR 已申请的题目
            studentId -> {
                Long studentDepartmentId = dataPermissionUtil.getCurrentUserDepartmentId();
                if (studentDepartmentId != null) {
                    // 检查该学生是否已有选题申请（包括待审核、通过、驳回等所有状态的申请）
                    LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
                    selectionWrapper.eq(BizSelection::getStudentId, studentId)
                                   .eq(BizSelection::getIsDeleted, IsDelete.NOT_DELETED.getCode());
                    
                    long selectionCount = bizSelectionMapper.selectCount(selectionWrapper);
                    
                    if (selectionCount > 0) {
                        // 学生已申请选题，只查询该学生申请的题目
                        List<Long> appliedTopicIds = bizSelectionMapper.selectList(selectionWrapper)
                            .stream()
                            .map(BizSelection::getTopicId)
                            .toList();
                        
                        if (!appliedTopicIds.isEmpty()) {
                            wrapper.in(BizTopic::getId, appliedTopicIds);
                            log.info("学生已申请选题，只查询已申请的题目：studentId={}, appliedTopicIds={}", studentId, appliedTopicIds);
                        } else {
                            // 理论上不会出现，因为 selectionCount > 0
                            wrapper.eq(BizTopic::getDepartmentId, studentDepartmentId);
                            wrapper.eq(BizTopic::getStatus, TopicStatus.OPEN.getCode());
                            log.info("学生未申请选题，过滤本院系开放题目：departmentId={}", studentDepartmentId);
                        }
                    } else {
                        // 学生未申请选题，查询本院系开放的题目
                        wrapper.eq(BizTopic::getDepartmentId, studentDepartmentId);
                        wrapper.eq(BizTopic::getStatus, TopicStatus.OPEN.getCode());
                        log.info("学生未申请选题，过滤本院系开放题目：departmentId={}", studentDepartmentId);
                    }
                } else {
                    log.warn("学生用户未找到院系信息，无法过滤题目");
                }
            },
            // 教师：只查看自己的题目
            teacherId -> {
                wrapper.eq(BizTopic::getTeacherId, teacherId);
                log.info("教师查询题目，过滤本人题目：teacherId={}", teacherId);
            },
            // 院系管理员：只查看本院系的题目（排除草稿状态）
            departmentId -> {
                wrapper.eq(BizTopic::getDepartmentId, departmentId);
                // 排除草稿状态（0），只能看到审核中、开放、关闭状态的题目
                wrapper.ne(BizTopic::getStatus, TopicStatus.DRAFT.getCode());
                log.info("院系管理员查询题目，过滤本院系题目且排除草稿：departmentId={}", departmentId);
            }
        );
    }

    private void clearTopicCache(Long topicId) {
        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + topicId;
        cacheHelper.evictCache(cacheKey);
    }
}