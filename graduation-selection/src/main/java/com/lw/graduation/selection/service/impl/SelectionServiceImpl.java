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
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.SelectionStatus;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final SysUserMapper sysUserMapper;
    private final CacheHelper cacheHelper;

    @Override
    public IPage<SelectionVO> getSelectionPage(SelectionPageQueryDTO queryDTO) {
        log.info("分页查询选题列表: {}", queryDTO);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStudentId() != null, BizSelection::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizSelection::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getStatus() != null, BizSelection::getStatus, queryDTO.getStatus())
                .eq(BizSelection::getIsDeleted, 0)
                .orderByDesc(BizSelection::getCreatedAt);

        // 2. 执行分页查询
        IPage<BizSelection> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizSelection> selectionPage = bizSelectionMapper.selectPage(page, wrapper);

        // 3. 转换为VO并填充关联信息
        IPage<SelectionVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(selectionPage.getRecords().stream()
                .map(this::convertToSelectionVO)
                .collect(Collectors.toList()));
        voPage.setTotal(selectionPage.getTotal());

        return voPage;
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO applySelection(SelectionApplyDTO applyDTO, Long studentId) {
        log.info("学生 {} 申请选题: {}", studentId, applyDTO.getTopicId());
        
        // 1. 验证题目是否存在且可选
        BizTopic topic = bizTopicMapper.selectById(applyDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }
        
        if (topic.getStatus() != 1) { // 非开放状态
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "题目当前不可选择");
        }
        
        // 2. 检查学生是否已申请过该题目
        LambdaQueryWrapper<BizSelection> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(BizSelection::getStudentId, studentId)
                   .eq(BizSelection::getTopicId, applyDTO.getTopicId())
                   .eq(BizSelection::getIsDeleted, 0);
        
        if (count(existWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "您已申请过该题目");
        }
        
        // 3. 检查题目是否还有名额
        if (topic.getSelectedCount() >= topic.getMaxSelections()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该题目已满员");
        }
        
        // 4. 创建选题申请记录
        BizSelection selection = new BizSelection();
        selection.setStudentId(studentId);
        selection.setTopicId(applyDTO.getTopicId());
        selection.setTopicTitle(topic.getTitle());
        selection.setStatus(SelectionStatus.PENDING_REVIEW.getValue()); // 待审核状态
        
        boolean saved = save(selection);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题申请失败");
        }
        
        // 5. 清除相关缓存
        clearSelectionCache(selection.getId());
        
        log.info("选题申请成功，ID: {}", selection.getId());
        return convertToSelectionVO(selection);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO reviewSelection(SelectionReviewDTO reviewDTO, Long teacherId) {
        log.info("教师 {} 审核选题: {}, 结果: {}", teacherId, reviewDTO.getSelectionId(), reviewDTO.getReviewResult());
        
        // 1. 获取选题申请信息
        BizSelection selection = getById(reviewDTO.getSelectionId());
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题申请不存在");
        }
        
        // 2. 验证审核权限（必须是该题目的指导教师）
        BizTopic topic = bizTopicMapper.selectById(selection.getTopicId());
        if (topic == null || !topic.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权审核该选题申请");
        }
        
        // 3. 验证选题状态
        SelectionStatus currentStatus = SelectionStatus.getByValue(selection.getStatus());
        if (currentStatus == null || currentStatus.isFinalStatus()) {
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
        
        // 5. 如果审核通过，更新题目选中人数
        if (reviewDTO.getReviewResult() == SelectionStatus.APPROVED.getValue()) {
            topic.setSelectedCount(topic.getSelectedCount() + 1);
            bizTopicMapper.updateById(topic);
        }
        
        // 6. 清除缓存
        clearSelectionCache(selection.getId());
        
        log.info("选题审核完成，ID: {}", selection.getId());
        return convertToSelectionVO(selection);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelectionVO confirmSelection(Long selectionId, Long studentId) {
        log.info("学生 {} 确认选题: {}", studentId, selectionId);
        
        // 1. 获取选题信息
        BizSelection selection = getById(selectionId);
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题不存在");
        }
        
        // 2. 验证确认权限
        if (!selection.getStudentId().equals(studentId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权确认他人选题");
        }
        
        // 3. 验证选题状态
        if (selection.getStatus() != SelectionStatus.APPROVED.getValue()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有审核通过的选题才能确认");
        }
        
        // 4. 更新确认状态
        selection.setStatus(SelectionStatus.CONFIRMED.getValue());
        selection.setConfirmedAt(LocalDateTime.now());
        
        boolean updated = updateById(selection);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题确认失败");
        }
        
        // 5. 清除缓存
        clearSelectionCache(selectionId);
        
        log.info("选题确认成功，ID: {}", selectionId);
        return convertToSelectionVO(selection);
    }

    @Override
    public List<SelectionVO> getSelectionsByStudent(Long studentId) {
        LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSelection::getStudentId, studentId)
               .eq(BizSelection::getIsDeleted, 0)
               .orderByDesc(BizSelection::getCreatedAt);
        
        return list(wrapper).stream()
                .map(this::convertToSelectionVO)
                .collect(Collectors.toList());
    }

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
                .collect(Collectors.toList());
        
        // 查询这些题目下的待审核选题申请
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.in(BizSelection::getTopicId, topicIds)
                       .eq(BizSelection::getStatus, SelectionStatus.PENDING_REVIEW.getValue())
                       .eq(BizSelection::getIsDeleted, 0)
                       .orderByAsc(BizSelection::getCreatedAt);
        
        return list(selectionWrapper).stream()
                .map(this::convertToSelectionVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSelection(Long selectionId, Long studentId) {
        log.info("学生 {} 撤销选题申请: {}", studentId, selectionId);
        
        // 1. 获取选题信息
        BizSelection selection = getById(selectionId);
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题申请不存在");
        }
        
        // 2. 验证撤销权限
        if (!selection.getStudentId().equals(studentId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权撤销他人选题申请");
        }
        
        // 3. 验证选题状态（只能撤销未确认的申请）
        SelectionStatus status = SelectionStatus.getByValue(selection.getStatus());
        if (status == null || status == SelectionStatus.CONFIRMED) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已确认的选题无法撤销");
        }
        
        // 4. 如果是已通过的申请，需要减少题目选中人数
        if (status == SelectionStatus.APPROVED) {
            BizTopic topic = bizTopicMapper.selectById(selection.getTopicId());
            if (topic != null && topic.getSelectedCount() > 0) {
                topic.setSelectedCount(topic.getSelectedCount() - 1);
                bizTopicMapper.updateById(topic);
            }
        }
        
        // 5. 逻辑删除选题申请
        boolean removed = removeById(selectionId);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "选题撤销失败");
        }
        
        // 6. 清除缓存
        clearSelectionCache(selectionId);
        
        log.info("选题撤销成功，ID: {}", selectionId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSelection(Long id, Long userId) {
        log.info("用户 {} 删除选题记录: {}", userId, id);
        
        // 1. 获取选题信息
        BizSelection selection = getById(id);
        if (selection == null || selection.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题记录不存在");
        }
        
        // 2. 验证删除权限（学生只能删除自己的，教师和管理员可以删除相关记录）
        BizStudent student = bizStudentMapper.selectById(selection.getStudentId());
        if (student != null && student.getUserId().equals(userId)) {
            // 学生删除自己的申请
            return cancelSelection(id, selection.getStudentId());
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
        return true;
    }

    /**
     * 转换选题实体为VO
     */
    private SelectionVO convertToSelectionVO(BizSelection selection) {
        SelectionVO vo = BeanMapperUtil.copyProperties(selection, SelectionVO.class);
        
        // 填充状态描述
        SelectionStatus status = SelectionStatus.getByValue(selection.getStatus());
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
        }
        
        return vo;
    }

    /**
     * 根据用户ID获取用户名
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
}