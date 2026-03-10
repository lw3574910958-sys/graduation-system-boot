package com.lw.graduation.notice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
import com.lw.graduation.common.service.WebSocketMessageService;
import com.lw.graduation.common.util.BeanMapperUtil;
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
    private final WebSocketMessageService webSocketMessageService;

    @Override
    public IPage<NoticeVO> getNoticePage(NoticePageQueryDTO queryDTO) {
        log.info("分页查询通知列表：{}", queryDTO);
    
        // 构建查询条件
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
                .orderByAsc(BizNotice::getCreatedAt);
    
        IPage<BizNotice> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizNotice> noticePage = bizNoticeMapper.selectPage(page, wrapper);
    
        // 过滤生效时间和目标范围
        IPage<NoticeVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        List<NoticeVO> filteredRecords = noticePage.getRecords().stream()
                .filter(this::filterByTargetScope)
                .filter(notice -> filterByEffectiveTime(notice, queryDTO.getEffectiveStatus()))
                .map(this::convertToNoticeVO)
                .collect(Collectors.toList());
            
        voPage.setRecords(filteredRecords);
        // 使用过滤后的实际记录数作为总数，确保分页信息准确
        voPage.setTotal(filteredRecords.size());
    
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
        log.info("用户 {} 创建通知：{}", publisherId, createDTO.getTitle());
    
        // 验证生效时间逻辑
        if (createDTO.getStartTime() != null && createDTO.getEndTime() != null) {
            if (createDTO.getStartTime().isAfter(createDTO.getEndTime())) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                    "生效开始时间不能晚于结束时间");
            }
        }
    
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
            log.warn("创建通知时状态设置异常，状态：{}", notice.getStatus());
        }
    
        boolean saved = save(notice);
        if (!saved) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知创建失败");
        }
    
        clearNoticeCache(notice.getId());
            
        // 如果立即发布，发送 WebSocket 通知
        if (Boolean.TRUE.equals(createDTO.getPublishNow())) {
            sendNoticeWebSocket(notice, publisherId);
        }
            
        return convertToNoticeVO(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(Long id, NoticeUpdateDTO updateDTO, Long updaterId) {
        log.info("用户 {} 更新通知：{}", updaterId, id);
    
        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }
    
        // 使用实体类的状态检查方法
        if (notice.isWithdrawn()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已撤回的通知不能编辑，请重新发布");
        }
        if (!notice.isEditable()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有草稿状态的通知才能编辑");
        }
    
        // 验证生效时间逻辑
        if (updateDTO.getStartTime() != null && updateDTO.getEndTime() != null) {
            if (updateDTO.getStartTime().isAfter(updateDTO.getEndTime())) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                    "生效开始时间不能晚于结束时间");
            }
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
        log.info("用户 {} 发布通知：{}", publisherId, id);
    
        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }
    
        // 统一使用 BizNotice 实体类的状态检查方法
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
            
        // 只有在生效时间已到或没有设置生效时间时，才发送 WebSocket 通知
        if (notice.isEffective()) {
            sendNoticeWebSocket(notice, publisherId);
        } else {
            log.info("通知 {} 已发布但尚未到生效时间，暂不发送 WebSocket 通知", id);
        }
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

    /**
     * 根据目标范围过滤通知
     * 注意：管理员可以看到所有通知，学生/教师/管理员用户只能看到对应范围的通知
     *
     * @param notice 通知对象
     * @return 是否符合目标范围
     */
    private boolean filterByTargetScope(BizNotice notice) {
        // 如果目标范围为 0（全体），则所有人都可以看到
        if (notice.getTargetScope() == null || notice.getTargetScope() == 0) {
            return true;
        }
        
        // 从查询条件中获取当前用户类型
        // 0-学生，1-教师，2-管理员
        Integer currentUserType = getCurrentUserType();
        
        // 根据目标范围判断当前用户是否可以看到
        // targetScope: 1-学生，2-教师，3-管理员
        if (notice.getTargetScope() == 1) {
            // 目标是学生：只有学生可以看到
            return currentUserType != null && currentUserType == 0;
        } else if (notice.getTargetScope() == 2) {
            // 目标是教师：只有教师可以看到
            return currentUserType != null && currentUserType == 1;
        } else if (notice.getTargetScope() == 3) {
            // 目标是管理员：只有管理员可以看到
            return currentUserType != null && currentUserType == 2;
        }
        
        return true;
    }
    
    /**
     * 获取当前用户类型
     * 通过 StpUtil 获取当前登录用户 ID，然后查询用户类型
     * @return 用户类型：0-学生，1-教师，2-管理员，null-获取失败
     */
    private Integer getCurrentUserType() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            if (userId != null) {
                SysUser user = sysUserMapper.selectById(userId);
                if (user != null && user.getUserType() != null) {
                    String userType = user.getUserType();
                    // 将用户类型转换为内部表示：0-学生，1-教师，2-管理员
                    if ("student".equals(userType)) {
                        return 0;
                    } else if ("teacher".equals(userType)) {
                        return 1;
                    } else if ("admin".equals(userType)) {
                        return 2;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户类型失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据生效时间过滤通知
     * 草稿状态的通知不受生效时间限制，始终显示
     * 已发布状态的通知始终显示（用于管理员查看和管理），但可以通过 isEffective() 判断是否在有效期内
     * 已撤回状态的通知始终显示
     *
     * @param notice 通知对象
     * @param effectiveStatus 生效状态过滤条件：effective-生效中，pending-待生效，expired-已过期，null-不过滤
     * @return 是否应该显示
     */
    private boolean filterByEffectiveTime(BizNotice notice, String effectiveStatus) {
        // 如果没有指定生效状态过滤，显示所有通知
        if (effectiveStatus == null || effectiveStatus.isEmpty()) {
            return true;
        }
        
        // 草稿和已撤回状态的通知，如果指定了生效状态过滤，只显示对应的状态
        if (notice.getStatus() != null && 
            (notice.getStatus() == NoticeStatus.DRAFT.getValue() || 
             notice.getStatus() == NoticeStatus.WITHDRAWN.getValue())) {
            // 草稿和已撤回状态不显示生效状态，所以不过滤
            return true;
        }
        
        // 判断通知的生效状态
        boolean isEffective = notice.isEffective();
        LocalDateTime now = LocalDateTime.now();
        
        if ("effective".equals(effectiveStatus)) {
            // 过滤生效中的通知
            return isEffective;
        } else if ("pending".equals(effectiveStatus)) {
            // 过滤待生效的通知（当前时间在开始时间之前）
            return notice.getStartTime() != null && now.isBefore(notice.getStartTime());
        } else if ("expired".equals(effectiveStatus)) {
            // 过滤已过期的通知（当前时间在结束时间之后）
            return notice.getEndTime() != null && now.isAfter(notice.getEndTime());
        }
        
        return true;
    }

    private NoticeVO convertToNoticeVO(BizNotice notice) {
        // 使用 BeanMapperUtil 简化对象转换
        NoticeVO vo = BeanMapperUtil.copyProperties(notice, NoticeVO.class);
    
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

    /**
     * 发送 WebSocket 通知
     */
    private void sendNoticeWebSocket(BizNotice notice, Long publisherId) {
        try {
            // 获取发布者信息
            SysUser publisher = sysUserMapper.selectById(publisherId);
            String publisherName = publisher != null ? publisher.getRealName() : "未知用户";
            
            // 发送 WebSocket 通知
            webSocketMessageService.sendNoticeNotification(
                notice.getId(),
                notice.getTitle(),
                notice.getType(),
                notice.getTargetScope(),
                publisherName
            );
        } catch (Exception e) {
            log.error("发送 WebSocket 通知失败：{}", e.getMessage(), e);
            // WebSocket 发送失败不影响主业务流程
        }
    }
}