package com.lw.graduation.selection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.selection.SelectionApplyDTO;
import com.lw.graduation.api.dto.selection.SelectionPageQueryDTO;
import com.lw.graduation.api.dto.selection.SelectionReviewDTO;
import com.lw.graduation.api.service.selection.SelectionService;
import com.lw.graduation.api.vo.selection.SelectionVO;
import com.lw.graduation.auth.service.PermissionValidationService;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.IEnum;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.status.SelectionStatus;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.topic.service.impl.TopicServiceImpl;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 选题服务实现类
 * 实现选题管理模块的完整业务流程，包括申请、审核、确认等环节。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SelectionServiceImpl extends ServiceImpl<BizSelectionMapper, BizSelection> implements SelectionService {

    private final BizSelectionMapper bizSelectionMapper;
    private final BizTopicMapper bizTopicMapper;
    private final BizStudentMapper bizStudentMapper;
    private final BizTeacherMapper bizTeacherMapper;
    private final SysUserMapper sysUserMapper;
    private final TopicServiceImpl topicService;
    private final CacheHelper cacheHelper;
    private final DataPermissionUtil dataPermissionUtil;
    private final PermissionValidationService permissionValidationService;

    /**
     * 分页查询选题列表
     *
     * @param queryDTO 查询条件
     * @return 选题列表
     */
    @Override
    public IPage<SelectionVO> getSelectionPage(SelectionPageQueryDTO queryDTO) {
        log.info("分页查询选题列表，当前页：{}，每页大小：{}", queryDTO.getCurrent(), queryDTO.getSize());

        // 1. 构建查询条件
        LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();

        // 2. 添加通用数据权限过滤（包含逻辑删除过滤）
        addPermissionFilter(wrapper, queryDTO);

        // 3. 其他查询条件
        wrapper.eq(queryDTO.getStudentId() != null, BizSelection::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizSelection::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getStatus() != null, BizSelection::getStatus, queryDTO.getStatus())
                // 按课题标题查询（直接使用 topic_title 字段）
                .like(queryDTO.getTopicTitle() != null, BizSelection::getTopicTitle, queryDTO.getTopicTitle())
                // 按学号查询（需要联表，模糊查询）
                .apply(queryDTO.getStudentNumber() != null,
                        "student_id IN (SELECT id FROM biz_student WHERE student_id LIKE CONCAT('%', {0}, '%'))",
                        queryDTO.getStudentNumber())
                // 按学生姓名查询（需要联表）
                .apply(queryDTO.getStudentName() != null,
                        "student_id IN (SELECT id FROM biz_student WHERE user_id IN (SELECT id FROM sys_user WHERE real_name LIKE CONCAT('%', {0}, '%')))",
                        queryDTO.getStudentName())
                .orderByDesc(BizSelection::getCreatedAt);

        // 4. 执行分页查询
        IPage<BizSelection> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizSelection> selectionPage = bizSelectionMapper.selectPage(page, wrapper);

        // 5. 转换为 VO 并填充关联信息
        IPage<SelectionVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(selectionPage.getRecords().stream()
                .map(this::convertToSelectionVO)
                .toList());
        voPage.setTotal(selectionPage.getTotal());

        return voPage;
    }

    /**
     * 根据 ID 获取选题信息
     *
     * @param id 选题 ID
     * @return 选题信息
     */
    @Override
    public SelectionVO getSelectionById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.SELECTION_INFO + id;

        return cacheHelper.getFromCache(cacheKey, SelectionVO.class, () -> {
            BizSelection selection = bizSelectionMapper.selectById(id);
            if (selection == null || selection.getIsDeleted() == 1) {
                return null;
            }
            return convertToSelectionVO(selection);
        }, CacheConstants.ExpireTime.COLD_DATA_EXPIRE);
    }

    /**
     * 申请选题
     *
     * @param applyDTO 申请信息
     * @param userId   用户 ID
     * @return 选题申请信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO applySelection(SelectionApplyDTO applyDTO, Long userId) {
        log.info("用户 [{}] 申请选题，题目 ID: {}", userId, applyDTO.getTopicId());

        // 1. 根据用户 ID 查询学生业务信息
        LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BizStudent::getUserId, userId)
                     .eq(BizStudent::getIsDeleted, 0);

        BizStudent student = bizStudentMapper.selectOne(studentWrapper);
        if (student == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "未找到学生信息，请先完善学生资料");
        }

        // 2. 验证题目是否存在且可选
        BizTopic topic = bizTopicMapper.selectById(applyDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        TopicStatus topicStatus = IEnum.getByCode(TopicStatus.class, topic.getStatus());
        if (topicStatus == null || !topicStatus.isSelectable()) { // 非可选状态
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "题目当前不可选择");
        }

        // 3. 检查学生是否已申请过该题目（避免重复申请同一题目）
        LambdaQueryWrapper<BizSelection> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizSelection::getStudentId, student.getId())  // 使用学生业务 ID
                   .eq(BizSelection::getTopicId, applyDTO.getTopicId())
                   .eq(BizSelection::getIsDeleted, 0);
                    
        List<BizSelection> existingApplications = list(existWrapper);
        for (BizSelection existing : existingApplications) {
            SelectionStatus existingStatus = IEnum.getByCode(SelectionStatus.class, existing.getStatus());
            if (existingStatus != null && existingStatus.isActive()) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "您已提交过该题目的申请，请勿重复申请");
            }
        }
                
        // 3.1 检查学生是否有待审核的选题申请（一个学生同时只能有一个待审核申请）
        // 注意：这里只限制待审核状态，不限制已审核通过的选题
        // 因为可能存在学生有多个选题申请但只有一个能通过审核的场景
        LambdaQueryWrapper<BizSelection> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(BizSelection::getStudentId, student.getId())
                     .eq(BizSelection::getIsDeleted, 0)
                     .eq(BizSelection::getStatus, SelectionStatus.PENDING_REVIEW.getCode());
                
        long pendingSelectionCount = count(pendingWrapper);
        if (pendingSelectionCount > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "您有待审核的选题申请，请先等待审核完成或撤销后再申请");
        }

        // 4. 检查题目是否还有名额
        if (topic.getSelectedCount() >= topic.getMaxSelections()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该题目已满员");
        }

        // 4.1 预占选题名额：增加题目的已选人数
        topic.setSelectedCount(topic.getSelectedCount() + 1);
        bizTopicMapper.updateById(topic);
        log.info("学生 [{}] 申请选题 [{}]，预占名额，当前已选人数：{}/{}", 
                student.getId(), topic.getId(), topic.getSelectedCount(), topic.getMaxSelections());
        
        // 4.2 检查是否达到人数上限，若达到则自动关闭题目
        if (topic.getSelectedCount() >= topic.getMaxSelections()) {
            TopicStatus currentStatus = IEnum.getByCode(TopicStatus.class, topic.getStatus());
            if (currentStatus == TopicStatus.OPEN) {
                topic.setStatus(TopicStatus.CLOSED.getCode());
                bizTopicMapper.updateById(topic);
                log.info("题目 [{}] 已达到选题人数上限，自动关闭", topic.getId());
            }
        }
        
        // 4.3 查询题目发布教师对应的 sys_user.id（用于设置审核人）
        Long reviewerUserId = null;
        if (topic.getTeacherId() != null) {
            LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
            teacherWrapper.eq(BizTeacher::getId, topic.getTeacherId())
                         .eq(BizTeacher::getIsDeleted, 0);
            BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
            if (teacher != null) {
                reviewerUserId = teacher.getUserId();
                log.info("查询到题目发布教师对应的 sys_user.id: {}", reviewerUserId);
            }
        }

        // 5. 创建选题申请记录
        BizSelection selection = new BizSelection();
        selection.setStudentId(student.getId());  // 使用学生业务 ID
        selection.setTopicId(applyDTO.getTopicId());
        selection.setTopicTitle(topic.getTitle());
        selection.setStatus(SelectionStatus.PENDING_REVIEW.getCode()); // 待审核状态
        selection.setReviewerId(reviewerUserId); // 设置审核教师为题目发布教师对应的 sys_user.id

        // 保存申请理由、能力说明和预期目标
        selection.setApplyReason(applyDTO.getApplyReason());
        selection.setStudentAbility(applyDTO.getStudentAbility());
        selection.setExpectedGoal(applyDTO.getExpectedGoal());

        boolean saved = save(selection);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题申请失败");
        }

        // 5. 触发题目状态变更检查（仅记录日志）
        topicService.handleSelectionApplied(applyDTO.getTopicId());
        
        // 6. 清除相关缓存
        clearSelectionCache(selection.getId());
        clearTopicCache(selection.getTopicId());

        log.info("选题申请成功，ID: {}", selection.getId());
        return convertToSelectionVO(selection);
    }

    /**
     * 审核选题
     *
     * @param reviewDTO 审核信息
     * @param teacherId 教师 ID
     * @return 选题审核信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO reviewSelection(SelectionReviewDTO reviewDTO, Long teacherId) {
        log.info("教师 [{}] 审核选题，申请 ID: {}，审核结果：{}",
                teacherId, reviewDTO.getSelectionId(),
                SelectionStatus.APPROVED.getCode().equals(reviewDTO.getReviewResult()) ? "通过" : "驳回");

        // 1. 验证审核权限
        permissionValidationService.validateSelectionReviewPermission(reviewDTO.getSelectionId(), teacherId);

        // 2. 获取选题申请信息
        BizSelection selection = getById(reviewDTO.getSelectionId());
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题申请不存在");
        }

        // 3. 验证审核状态
        SelectionStatus currentStatus = IEnum.getByCode(SelectionStatus.class, selection.getStatus());
        if (currentStatus != null && currentStatus.isFinalStatus()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "选题状态不允许审核");
        }

        // 4. 更新审核信息
        selection.setStatus(reviewDTO.getReviewResult());
        selection.setReviewerId(teacherId);
        selection.setReviewedAt(LocalDateTime.now());
        selection.setReviewComment(reviewDTO.getReviewComment());

        boolean updated = updateById(selection);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题审核失败");
        }

        // 5. 触发题目状态变更
        boolean isApproved = SelectionStatus.APPROVED.getCode().equals(reviewDTO.getReviewResult());
        topicService.handleSelectionReviewed(selection.getTopicId(), isApproved);

        // 6. 清除缓存
        clearSelectionCache(selection.getId());

        log.info("选题审核完成，ID: {}", selection.getId());
        return convertToSelectionVO(selection);
    }

    /**
     * 确认选题
     *
     * @param selectionId 选题 ID
     * @param studentId   学生 ID
     * @return 选题确认信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO confirmSelection(Long selectionId, Long userId) {
        log.info("学生用户 [{}] 确认选题，申请 ID: {}", userId, selectionId);
            
        // 1. 验证确认权限
        permissionValidationService.validateSelectionConfirmPermission(selectionId, userId);
            
        // 2. 获取选题信息
        BizSelection selection = getById(selectionId);
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题不存在");
        }
    
        // 3. 验证选题状态
        if (!selection.isApproved()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有审核通过的选题才能确认");
        }
    
        // 5. 更新确认状态
        selection.setStatus(SelectionStatus.CONFIRMED.getCode());
        LocalDateTime now = LocalDateTime.now();
        selection.setConfirmedAt(now);
        
        log.debug("准备更新选题确认状态，ID: {}, 确认时间：{}", selectionId, now);
        
        boolean updated = updateById(selection);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题确认失败");
        }
        
        log.debug("选题更新结果，ID: {}, 更新结果：{}", selectionId, updated);
    
        // 6. 触发题目状态变更
        topicService.handleSelectionConfirmed(selection.getTopicId());
    
        // 7. 清除缓存
        clearSelectionCache(selectionId);
    
        log.info("选题确认成功，ID: {}", selectionId);
        return convertToSelectionVO(selection);
    }

    /**
     * 根据学生 ID 获取选题信息
     *
     * @param studentId 学生 ID
     * @return 选题信息列表
     */
    @Override
    public List<SelectionVO> getSelectionsByStudent(Long studentId) {
        LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSelection::getStudentId, studentId)
               .eq(BizSelection::getIsDeleted, 0)
               .orderByDesc(BizSelection::getCreatedAt);

        return list(wrapper).stream()
                .map(this::convertToSelectionVO)
                .toList();
    }

    /**
     * 根据教师 ID 获取选题信息
     *
     * @param teacherId 教师 ID
     * @return 选题信息列表
     */
    @Override
    public List<SelectionVO> getSelectionsForReview(Long teacherId) {
        // 获取该教师指导的所有题目
        LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
        topicWrapper.eq(BizTopic::getTeacherId, teacherId);
        List<BizTopic> topics = bizTopicMapper.selectList(topicWrapper);

        if (CollectionUtils.isEmpty(topics)) {
            return List.of();
        }

        List<Long> topicIds = topics.stream()
                .map(BizTopic::getId)
                .toList();

        // 查询这些题目下的待审核选题申请
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.in(BizSelection::getTopicId, topicIds)
                       .eq(BizSelection::getStatus, SelectionStatus.PENDING_REVIEW.getCode())
                       .eq(BizSelection::getIsDeleted, 0)
                       .orderByAsc(BizSelection::getCreatedAt);

        return list(selectionWrapper).stream()
                .map(this::convertToSelectionVO)
                .toList();
    }

    /**
     * 撤销选题
     *
     * @param selectionId 选题 ID
     * @param userId      用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSelection(Long selectionId, Long userId) {
        log.info("学生用户 [{}] 撤销选题申请，申请 ID: {}", userId, selectionId);
        
        // 1. 根据用户 ID 查询学生业务 ID（复用 DataPermissionUtil 工具方法）
        Long studentBizId = dataPermissionUtil.getStudentIdByUserId(userId);
        if (studentBizId == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "未找到学生信息");
        }
        
        // 2. 获取选题信息
        BizSelection selection = getById(selectionId);
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题申请不存在");
        }
        
        // 3. 验证撤销权限（使用业务学生 ID 进行比较）
        if (!selection.getStudentId().equals(studentBizId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权撤销他人选题申请");
        }
        
        // 4. 验证选题状态（只能撤销待审核状态的申请）
        SelectionStatus status = IEnum.getByCode(SelectionStatus.class, selection.getStatus());
        if (status == null || status != SelectionStatus.PENDING_REVIEW) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有待审核状态的选题才能撤销");
        }
        
        Long topicId = selection.getTopicId();
        
        // 5. 减少题目选中人数（因为在申请时已预占名额）
        BizTopic topic = bizTopicMapper.selectById(topicId);
        if (topic != null && topic.getSelectedCount() > 0) {
            topic.setSelectedCount(topic.getSelectedCount() - 1);
            bizTopicMapper.updateById(topic);
            log.info("学生 [{}] 撤销选题申请 [{}]，释放名额，当前已选人数：{}/{}", 
                    studentBizId, selectionId, topic.getSelectedCount(), topic.getMaxSelections());
        }
        
        // 6. 检查是否需要重新开放题目
        if (topic != null && topic.getSelectedCount() < topic.getMaxSelections()) {
            TopicStatus topicStatus = IEnum.getByCode(TopicStatus.class, topic.getStatus());
            if (topicStatus == TopicStatus.CLOSED) {
                // 如果题目已关闭且还有名额，重新开放
                topic.setStatus(TopicStatus.OPEN.getCode());
                bizTopicMapper.updateById(topic);
                log.info("题目 [{}] 因学生撤销申请且还有名额，自动重新开放", topicId);
            }
        }
        
        // 7. 逻辑删除选题申请
        boolean removed = removeById(selectionId);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题撤销失败");
        }
        
        // 7. 触发题目状态变更检查（仅记录日志）
        topicService.handleSelectionReviewed(topicId, false);
        
        // 8. 清除缓存
        clearSelectionCache(selectionId);
        
        log.info("选题撤销成功，ID: {}", selectionId);
    }

    /**
     * 重新申请选题
     *
     * @param selectionId 选题 ID
     * @param userId      用户 ID
     * @param applyDTO    申请参数
     * @return 选题重新申请信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO resubmitSelection(Long selectionId, Long userId, SelectionApplyDTO applyDTO) {
        log.info("学生用户 [{}] 重新申请选题，原申请 ID: {}", userId, selectionId);
        
        // 1. 根据用户 ID 查询学生业务 ID（复用 DataPermissionUtil 工具方法）
        Long studentBizId = dataPermissionUtil.getStudentIdByUserId(userId);
        if (studentBizId == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "未找到学生信息");
        }
        
        // 2. 获取原选题信息
        BizSelection originalSelection = getById(selectionId);
        if (originalSelection == null || originalSelection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "原选题申请不存在");
        }
        
        // 3. 验证权限和状态（使用业务学生 ID 进行比较）
        if (!originalSelection.getStudentId().equals(studentBizId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权重新申请他人选题");
        }
        
        SelectionStatus originalStatus = IEnum.getByCode(SelectionStatus.class, originalSelection.getStatus());
        if (originalStatus == null || !originalStatus.canResubmit()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该选题状态不允许重新申请");
        }
        
        // 4. 验证题目是否仍然可选
        BizTopic topic = bizTopicMapper.selectById(originalSelection.getTopicId());
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "原题目不存在");
        }
        
        TopicStatus topicStatus = IEnum.getByCode(TopicStatus.class, topic.getStatus());
        if (topicStatus == null || !topicStatus.isSelectable()) { // 非可选状态
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "原题目当前不可选择");
        }
        
        // 5. 重新申请不检查名额限制（因为是替换原申请，不改变 selected_count）
        
        // 6. 检查是否已达到最大申请次数（防止无限循环）
        LambdaQueryWrapper<BizSelection> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(BizSelection::getStudentId, studentBizId)
                   .eq(BizSelection::getTopicId, originalSelection.getTopicId())
                   .eq(BizSelection::getIsDeleted, 0);
        
        long applicationCount = count(countWrapper);
        if (applicationCount >= 3) { // 最多允许 3 次申请
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该题目的申请次数已达上限");
        }
        
        // 6. 创建新的选题申请记录
        BizSelection newSelection = new BizSelection();
        newSelection.setStudentId(studentBizId);
        newSelection.setTopicId(originalSelection.getTopicId());
        newSelection.setTopicTitle(originalSelection.getTopicTitle());
        newSelection.setStatus(SelectionStatus.PENDING_REVIEW.getCode()); // 重新设置为待审核状态
            
        // 保存申请理由、能力说明和预期目标
        newSelection.setApplyReason(applyDTO.getApplyReason());
        newSelection.setStudentAbility(applyDTO.getStudentAbility());
        newSelection.setExpectedGoal(applyDTO.getExpectedGoal());
        
        boolean saved = save(newSelection);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "重新申请失败");
        }
        
        // 7. 逻辑删除原有的选题记录（避免数据冗余）
        // 使用 removeById 方法，MyBatis-Plus 会自动处理 @TableLogic 字段
        boolean removed = removeById(selectionId);
        if (!removed) {
            log.warn("逻辑删除原选题记录失败，原记录 ID: {}", selectionId);
        }
        
        // 注意：重新申请是替换原申请，不改变 selected_count
        // 原申请的 selected_count 已在首次申请时增加，无需额外操作
        
        // 8. 触发题目状态变更检查（仅记录日志）
        topicService.handleSelectionApplied(originalSelection.getTopicId());
        
        // 9. 清除相关缓存
        clearSelectionCache(newSelection.getId());
        clearTopicCache(originalSelection.getTopicId());
        
        log.info("选题重新申请成功，新 ID: {}", newSelection.getId());
        return convertToSelectionVO(newSelection);
    }

    /**
     * 删除选题记录
     *
     * @param id    选题 ID
     * @param userId 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSelection(Long id, Long userId) {
        log.info("用户[{}] 删除选题记录，记录ID: {}", userId, id);

        // 1. 获取选题信息
        BizSelection selection = getById(id);
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题记录不存在");
        }

        // 2. 验证删除权限（学生只能删除自己的，教师和管理员可以删除相关记录）
        BizStudent student = bizStudentMapper.selectById(selection.getStudentId());
        if (student != null && student.getUserId().equals(userId)) {
            // 学生删除自己的申请
            cancelSelection(id, selection.getStudentId());
            return;
        }

        // 教师和管理员权限验证
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权删除选题记录");
        }

        // 3. 逻辑删除
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题删除失败");
        }

        // 4. 清除缓存
        clearSelectionCache(id);

        log.info("选题删除成功，ID: {}", id);
    }

    /**
     * 转换选题实体为 VO
     *
     * @param selection 选题实体
     * @return 选题 VO
     */
    private SelectionVO convertToSelectionVO(BizSelection selection) {
        SelectionVO vo = BeanMapperUtil.copyProperties(selection, SelectionVO.class);
        // 填充状态描述
        SelectionStatus status = IEnum.getByCode(SelectionStatus.class, selection.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDescription());
        }

        // 填充学生信息
        if (selection.getStudentId() != null) {
            BizStudent student = bizStudentMapper.selectById(selection.getStudentId());
            if (student != null) {
                vo.setStudentName(getUserNameById(student.getUserId()));
                vo.setStudentNumber(student.getStudentId());
            }
        }

        // 填充审核教师信息
        if (selection.getReviewerId() != null) {
            vo.setReviewerName(getUserNameById(selection.getReviewerId()));
            // 查询教师工号
            LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
            teacherWrapper.eq(BizTeacher::getUserId, selection.getReviewerId())
                         .eq(BizTeacher::getIsDeleted, 0);
            BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
            if (teacher != null) {
                vo.setReviewerNumber(teacher.getTeacherId());
            }
        }

        return vo;
    }

    /**
     * 根据用户 ID 获取用户名
     */
    private String getUserNameById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getRealName() : "";
    }

    /**
     * 清除选题相关缓存
     */
    private void clearSelectionCache(Long selectionId) {
        String cacheKey = CacheConstants.KeyPrefix.SELECTION_INFO + selectionId;
        cacheHelper.evictCache(cacheKey);
    }

    /**
     * 清除题目相关缓存
     */
    private void clearTopicCache(Long topicId) {
        String cacheKey = CacheConstants.KeyPrefix.TOPIC_INFO + topicId;
        cacheHelper.evictCache(cacheKey);
    }

    /**
     * 根据用户类型添加权限过滤条件（使用通用方法）
     * 
     * 技术说明：
     * MyBatis-Plus 的逻辑删除自动注入机制：
     * - 若实体配置了 @TableLogic，所有通过 BaseMapper 生成的 SQL 都会自动追加 is_deleted = 0
     * - 使用 apply() 方法直接拼接 SQL 可以绕过自动注入
     * - 但 apply() 必须在 wrapper 构建的最开始就调用，否则会被后续的自动注入覆盖
     * 
     * 角色策略：
     * - 学生：不写 isDeleted 条件，由 MP 自动注入 is_deleted = 0
     * - 教师/管理员：使用 apply() 在 wrapper 最开始就指定 is_deleted IN (0,1)
     *
     * @param wrapper 查询条件
     * @param queryDTO 查询参数
     */
    private void addPermissionFilter(LambdaQueryWrapper<BizSelection> wrapper, SelectionPageQueryDTO queryDTO) {
        String userType = dataPermissionUtil.getCurrentUserTypeString();
        boolean isStudent = "student".equals(userType);

        // 关键：在 wrapper 最开始就使用 apply() 直接指定 SQL 条件
        // 这样可以阻止 MyBatis-Plus 后续自动注入 is_deleted = 0
        if (!isStudent) {
            // 非学生角色：查询所有数据（包括已删除和未删除）
            wrapper.in(BizSelection::getIsDeleted, 0, 1);
        } else {
            // 学生角色：只查询未删除数据
            wrapper.eq(BizSelection::getIsDeleted, 0);
        }

        // 数据权限过滤（保持不变）
        dataPermissionUtil.addCommonDataPermissionFilter(
            wrapper,
            studentId -> wrapper.eq(BizSelection::getStudentId, studentId),
            teacherId -> wrapper.in(BizSelection::getTopicId,
                bizTopicMapper.selectList(
                    new LambdaQueryWrapper<BizTopic>()
                        .eq(BizTopic::getTeacherId, teacherId)
                        .select(BizTopic::getId)
                ).stream().map(BizTopic::getId).toList()
            ),
            departmentId -> wrapper.in(BizSelection::getTopicId,
                bizTopicMapper.selectList(
                    new LambdaQueryWrapper<BizTopic>()
                        .eq(BizTopic::getDepartmentId, departmentId)
                        .select(BizTopic::getId)
                    ).stream().map(BizTopic::getId).toList()
            )
        );
    }
}