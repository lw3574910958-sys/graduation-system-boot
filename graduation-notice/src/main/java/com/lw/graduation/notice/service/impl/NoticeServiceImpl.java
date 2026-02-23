package com.lw.graduation.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.notice.NoticeCreateDTO;
import com.lw.graduation.api.dto.notice.NoticePageQueryDTO;
import com.lw.graduation.api.dto.notice.NoticeUpdateDTO;
import com.lw.graduation.api.service.notice.NoticeService;
import com.lw.graduation.api.vo.notice.NoticeVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.notice.BizNotice;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.notice.NoticeStatus;
import com.lw.graduation.domain.enums.notice.NoticeType;
import com.lw.graduation.infrastructure.mapper.notice.BizNoticeMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * 实现通知公告管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl extends ServiceImpl<BizNoticeMapper, BizNotice> implements NoticeService {

    private final BizNoticeMapper bizNoticeMapper;
    private final SysUserMapper sysUserMapper;
    private final CacheHelper cacheHelper;

    @Override
    public IPage<NoticeVO> getNoticePage(NoticePageQueryDTO queryDTO) {
        log.info("分页查询通知列表: {}", queryDTO);

        LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getTitle() != null, BizNotice::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getType() != null, BizNotice::getType, queryDTO.getType())
                .eq(queryDTO.getPriority() != null, BizNotice::getPriority, queryDTO.getPriority())
                .eq(queryDTO.getStatus() != null, BizNotice::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getIsSticky() != null, BizNotice::getIsSticky, queryDTO.getIsSticky())
                .eq(queryDTO.getTargetScope() != null, BizNotice::getTargetScope, queryDTO.getTargetScope())
                .eq(queryDTO.getPublisherId() != null, BizNotice::getPublisherId, queryDTO.getPublisherId())
                .eq(BizNotice::getIsDeleted, 0)
                .orderByDesc(BizNotice::getIsSticky)
                .orderByDesc(BizNotice::getPublishedAt)
                .orderByDesc(BizNotice::getCreatedAt);

        IPage<BizNotice> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizNotice> noticePage = bizNoticeMapper.selectPage(page, wrapper);

        IPage<NoticeVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(noticePage.getRecords().stream()
                .map(this::convertToNoticeVO)
                .collect(Collectors.toList()));
        voPage.setTotal(noticePage.getTotal());

        return voPage;
    }

    @Override
    public NoticeVO getNoticeById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.NOTICE_INFO + id;

        return cacheHelper.getFromCache(cacheKey, NoticeVO.class, () -> {
            BizNotice notice = bizNoticeMapper.selectById(id);
            if (notice == null || notice.getIsDeleted() == 1) {
                return null;
            }

            // 检查通知是否已被撤回
            if (notice.isWithdrawn()) {
                log.debug("通知 {} 已被撤回，但仍可查看详情", id);
            }

            return convertToNoticeVO(notice);
        }, CacheConstants.ExpireTime.COLD_DATA_EXPIRE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO createNotice(NoticeCreateDTO createDTO, Long publisherId) {
        log.info("用户 {} 创建通知: {}", publisherId, createDTO.getTitle());

        BizNotice notice = new BizNotice();
        notice.setTitle(createDTO.getTitle());
        notice.setContent(createDTO.getContent());
        notice.setType(createDTO.getType());
        notice.setPriority(createDTO.getPriority() != null ? createDTO.getPriority() : 2);
        notice.setPublisherId(publisherId);
        notice.setStartTime(createDTO.getStartTime());
        notice.setEndTime(createDTO.getEndTime());
        notice.setIsSticky(createDTO.getIsSticky() != null ? createDTO.getIsSticky() : 0);
        notice.setTargetScope(createDTO.getTargetScope() != null ? createDTO.getTargetScope() : 0);
        notice.setAttachmentUrl(createDTO.getAttachmentUrl());
        notice.setReadCount(0);

        // 设置初始状态
        if (Boolean.TRUE.equals(createDTO.getPublishNow())) {
            notice.setStatus(NoticeStatus.PUBLISHED.getValue());
            notice.setPublishedAt(LocalDateTime.now());
        } else {
            notice.setStatus(NoticeStatus.DRAFT.getValue());
        }

        // 验证状态设置的合理性
        BizNotice tempNotice = new BizNotice();
        tempNotice.setStatus(notice.getStatus());
        if (!tempNotice.isEditable() && !Boolean.TRUE.equals(createDTO.getPublishNow())) {
            log.warn("创建通知时状态设置异常，状态: {}", notice.getStatus());
        }

        boolean saved = save(notice);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知创建失败");
        }

        clearNoticeCache(notice.getId());
        return convertToNoticeVO(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(Long id, NoticeUpdateDTO updateDTO, Long updaterId) {
        log.info("用户 {} 更新通知: {}", updaterId, id);

        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }

        // 使用实体类的状态检查方法
        if (notice.isWithdrawn()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已撤回的通知不能编辑，请重新发布");
        }
        if (!notice.isEditable()) {  // 使用新方法名
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的通知才能编辑");
        }

        notice.setTitle(updateDTO.getTitle());
        notice.setContent(updateDTO.getContent());
        notice.setType(updateDTO.getType());
        notice.setPriority(updateDTO.getPriority());
        notice.setStartTime(updateDTO.getStartTime());
        notice.setEndTime(updateDTO.getEndTime());
        notice.setIsSticky(updateDTO.getIsSticky());
        notice.setTargetScope(updateDTO.getTargetScope());
        notice.setAttachmentUrl(updateDTO.getAttachmentUrl());

        boolean updated = updateById(notice);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知更新失败");
        }

        clearNoticeCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(Long id, Long publisherId) {
        log.info("用户 {} 发布通知: {}", publisherId, id);

        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }

        // 统一使用BizNotice实体类的状态检查方法
        if (!notice.canPublish()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的通知才能发布");
        }

        notice.setStatus(NoticeStatus.PUBLISHED.getValue());
        notice.setPublishedAt(LocalDateTime.now());

        boolean updated = updateById(notice);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知发布失败");
        }

        clearNoticeCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawNotice(Long id, Long publisherId) {
        log.info("用户 {} 撤回通知: {}", publisherId, id);

        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }

        // 统一使用BizNotice实体类的状态检查方法
        if (!notice.canWithdraw()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有已发布的通知才能撤回");
        }

        notice.setStatus(NoticeStatus.WITHDRAWN.getValue());

        boolean updated = updateById(notice);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知撤回失败");
        }

        clearNoticeCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id, Long userId) {
        log.info("用户 {} 删除通知: {}", userId, id);

        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }

        // 统一使用BizNotice实体类的状态检查方法 - 已发布的通知不能直接删除
        if (notice.isFinalStatus()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已发布的通知不能直接删除，请先撤回");
        }

        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知删除失败");
        }

        clearNoticeCache(id);

        log.info("通知删除成功，ID: {}", id);
    }

    @Override
    public List<NoticeVO> getStickyNotices(Integer targetScope) {
        LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getValue())
                .eq(targetScope != null, BizNotice::getTargetScope, targetScope)
                .eq(BizNotice::getIsDeleted, 0)
                .orderByDesc(BizNotice::getPublishedAt);

        return list(wrapper).stream()
                .filter(BizNotice::isSticky)  // 使用实体类的isSticky()方法
                .filter(BizNotice::isEffective) // 使用实体类的isEffective()方法
                .map(this::convertToNoticeVO)
                .toList();
    }

    @Override
    public List<NoticeVO> getLatestNotices(Integer targetScope, Integer size) {
        LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getValue())
                .eq(targetScope != null, BizNotice::getTargetScope, targetScope)
                .eq(BizNotice::getIsDeleted, 0)
                .orderByDesc(BizNotice::getPublishedAt);

        return list(wrapper).stream()
                .filter(BizNotice::isEffective) // 使用实体类的isEffective()方法
                .limit(size != null && size > 0 ? size : Long.MAX_VALUE)
                .map(this::convertToNoticeVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer increaseReadCount(Long id) {
        BizNotice notice = getById(id);
        if (notice == null) {
            return 0;
        }

        Integer newCount = notice.getReadCount() != null ? notice.getReadCount() + 1 : 1;
        notice.setReadCount(newCount);
        updateById(notice);

        clearNoticeCache(id);
        return newCount;
    }

    private NoticeVO convertToNoticeVO(BizNotice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setType(notice.getType());
        vo.setPriority(notice.getPriority());
        vo.setPublisherId(notice.getPublisherId());
        vo.setPublishedAt(notice.getPublishedAt());
        vo.setStartTime(notice.getStartTime());
        vo.setEndTime(notice.getEndTime());
        vo.setStatus(notice.getStatus());
        vo.setIsSticky(notice.getIsSticky()); // 使用实体类的isSticky()方法对应的字段
        vo.setReadCount(notice.getReadCount());
        vo.setTargetScope(notice.getTargetScope());
        vo.setAttachmentUrl(notice.getAttachmentUrl());
        vo.setCreatedAt(notice.getCreatedAt());
        vo.setUpdatedAt(notice.getUpdatedAt());

        // 填充描述信息
        NoticeType type = NoticeType.getByValue(notice.getType());
        if (type != null) {
            vo.setTypeDesc(type.getDescription());
        }

        NoticeStatus status = NoticeStatus.getByValue(notice.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDescription());
        }

        // 填充发布者信息
        if (notice.getPublisherId() != null) {
            SysUser publisher = sysUserMapper.selectById(notice.getPublisherId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getRealName());
            }
        }

        // 添加置顶状态的额外信息
        if (notice.isSticky()) {
            log.debug("通知 {} 为置顶通知", notice.getId());
        }

        return vo;
    }

    private void clearNoticeCache(Long noticeId) {
        String cacheKey = CacheConstants.KeyPrefix.NOTICE_INFO + noticeId;
        cacheHelper.evictCache(cacheKey);
    }
}