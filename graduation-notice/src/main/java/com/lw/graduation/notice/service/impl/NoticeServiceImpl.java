package com.lw.graduation.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.api.dto.notice.NoticeCreateDTO;
import com.lw.graduation.api.dto.notice.NoticePageQueryDTO;
import com.lw.graduation.api.dto.notice.NoticeUpdateDTO;
import com.lw.graduation.api.service.notice.NoticeService;
import com.lw.graduation.api.vo.notice.NoticeVO;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.IEnum;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.admin.BizAdmin;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.domain.entity.notice.BizNotice;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.notice.NoticeStatus;
import com.lw.graduation.domain.enums.notice.NoticeType;
import com.lw.graduation.infrastructure.mapper.admin.BizAdminMapper;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.infrastructure.mapper.notice.BizNoticeMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final BizAdminMapper bizAdminMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final CacheHelper cacheHelper;
    private final DataPermissionUtil dataPermissionUtil;

    @Override
    public IPage<NoticeVO> getNoticePage(NoticePageQueryDTO queryDTO) {
        log.info("分页查询通知列表：{}", queryDTO);
    
        // 1. 构建查询条件
        LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getTitle() != null, BizNotice::getTitle, queryDTO.getTitle())
                .eq(queryDTO.getType() != null, BizNotice::getType, queryDTO.getType())
                .eq(queryDTO.getPriority() != null, BizNotice::getPriority, queryDTO.getPriority())
                .eq(queryDTO.getStatus() != null, BizNotice::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getIsSticky() != null, BizNotice::getIsSticky, queryDTO.getIsSticky())
                .eq(queryDTO.getTargetScope() != null, BizNotice::getTargetScope, queryDTO.getTargetScope())
                .eq(queryDTO.getDepartmentId() != null, BizNotice::getDepartmentId, queryDTO.getDepartmentId())
                .eq(BizNotice::getIsDeleted, 0)
                .orderByDesc(BizNotice::getIsSticky)
                .orderByAsc(BizNotice::getCreatedAt);
    
        // 2. 添加通用数据权限过滤（按目标范围）
        addPermissionFilter(wrapper, queryDTO);
    
        // 3. 执行分页查询
        IPage<BizNotice> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizNotice> noticePage = bizNoticeMapper.selectPage(page, wrapper);
            
        // 4. 转换为 VO 并过滤生效时间（内存过滤保留，因为时间过滤复杂）
        IPage<NoticeVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(noticePage.getRecords().stream()
                .filter(notice -> autoWithdrawExpiredNotice(notice)) // 自动撤回过期公告
                .filter(notice -> filterByEffectiveTime(notice, queryDTO.getEffectiveStatus()))
                .map(this::convertToNoticeVO)
                .toList());
        voPage.setTotal(noticePage.getTotal()); // 使用数据库的总数
    
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
    
        // 1. 验证目标范围和院系的匹配关系
        validateTargetScopeAndDepartment(createDTO, publisherId);
    
        // 2. 验证生效时间逻辑
        validateEffectiveTime(createDTO.getStartTime(), createDTO.getEndTime());
    
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
        notice.setDepartmentId(createDTO.getDepartmentId()); // 设置院系 ID
        notice.setAttachmentUrl(createDTO.getAttachmentUrl());
        notice.setReadCount(0);
    
        // 设置初始状态
        if (Boolean.TRUE.equals(createDTO.getPublishNow())) {
            notice.setStatus(NoticeStatus.PUBLISHED.getCode());
            notice.setPublishedAt(LocalDateTime.now());
        } else {
            notice.setStatus(NoticeStatus.DRAFT.getCode());
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
    
        // 1. 验证目标范围和院系的匹配关系
        validateTargetScopeAndDepartment(updateDTO, updaterId);
    
        // 2. 验证生效时间逻辑
        validateEffectiveTime(updateDTO.getStartTime(), updateDTO.getEndTime());
    
        notice.setTitle(updateDTO.getTitle());
        notice.setContent(updateDTO.getContent());
        notice.setType(updateDTO.getType());
        notice.setPriority(updateDTO.getPriority());
        notice.setStartTime(updateDTO.getStartTime());
        notice.setEndTime(updateDTO.getEndTime());
        notice.setIsSticky(updateDTO.getIsSticky());
        notice.setTargetScope(updateDTO.getTargetScope());
        notice.setDepartmentId(updateDTO.getDepartmentId()); // 更新院系 ID
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
    
        notice.setStatus(NoticeStatus.PUBLISHED.getCode());
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

        notice.setStatus(NoticeStatus.WITHDRAWN.getCode());

        boolean updated = updateById(notice);
        if (!updated) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "通知撤回失败");
        }

        clearNoticeCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id, Long userId) {
        log.info("用户 {} 删除通知：{}", userId, id);
    
        BizNotice notice = getById(id);
        if (notice == null || notice.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "通知不存在");
        }
    
        // 只有已撤回状态的通知才能删除
        if (notice.getStatus() != NoticeStatus.WITHDRAWN.getCode()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "只有已撤回的通知才能删除");
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
        log.info("获取置顶通知列表，targetScope={}", targetScope);
            
        // 1. 获取当前用户类型和院系 ID
        Integer currentUserType = dataPermissionUtil.getCurrentUserTypeCode();
        Long currentDepartmentId = dataPermissionUtil.getCurrentUserDepartmentId();
            
        // 2. 如果是系统管理员，不显示任何公告（系统管理员不再接收通知）
        if (currentUserType != null && currentUserType == 2) {
            log.info("系统管理员不接收公告通知，返回空列表");
            return List.of();
        }
            
        // 3. 构建查询条件
        LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getCode()) // 已发布
                .eq(BizNotice::getIsDeleted, 0) // 未删除
                .orderByDesc(BizNotice::getPublishedAt);
            
        // 4. 使用新的目标范围和院系双重过滤逻辑
        addTargetScopeAndDepartmentFilter(wrapper, currentUserType, currentDepartmentId);
            
        // 5. 执行查询并过滤
        return list(wrapper).stream()
                .filter(BizNotice::isSticky)  // 使用实体类的 isSticky() 方法
                .filter(notice -> autoWithdrawExpiredNotice(notice)) // 自动撤回过期公告
                .filter(BizNotice::isEffective) // 使用实体类的 isEffective() 方法
                .map(this::convertToNoticeVO)
                .toList();
    }

    @Override
    public List<NoticeVO> getLatestNotices(Integer targetScope, Integer size) {
        log.info("获取最新通知列表，targetScope={}, size={}", targetScope, size);
            
        // 1. 获取当前用户类型和院系 ID
        Integer currentUserType = dataPermissionUtil.getCurrentUserTypeCode();
        Long currentDepartmentId = dataPermissionUtil.getCurrentUserDepartmentId();
                
        // 2. 如果是系统管理员，不显示任何公告（系统管理员不再接收通知）
        if (currentUserType != null && currentUserType == 2) {
            log.info("系统管理员不接收公告通知，返回空列表");
            return List.of();
        }
            
        // 3. 构建查询条件
        LambdaQueryWrapper<BizNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizNotice::getStatus, NoticeStatus.PUBLISHED.getCode()) // 已发布
                .eq(BizNotice::getIsDeleted, 0) // 未删除
                .orderByDesc(BizNotice::getPublishedAt);
            
        // 4. 使用新的目标范围和院系双重过滤逻辑
        addTargetScopeAndDepartmentFilter(wrapper, currentUserType, currentDepartmentId);
            
        // 5. 执行查询并过滤生效时间
        return list(wrapper).stream()
                .filter(notice -> autoWithdrawExpiredNotice(notice)) // 自动撤回过期公告
                .filter(BizNotice::isEffective) // 只返回生效中的公告
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
     * 根据用户类型添加权限过滤条件（使用通用方法）
     * - 系统管理员：可以查看所有公告
     * - 院系管理员：只查看自己发布的公告
     * - 教师：无法查看公告列表
     * - 学生：无法查看公告列表
     *
     * @param wrapper 查询条件
     * @param queryDTO 查询参数
     */
    private void addPermissionFilter(LambdaQueryWrapper<BizNotice> wrapper, NoticePageQueryDTO queryDTO) {
        // 获取当前用户 ID（直接从 StpUtil 获取）
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        // 使用通用数据权限过滤方法
        dataPermissionUtil.addCommonDataPermissionFilter(
            wrapper,
            // 学生：无法查看公告列表（已在 Controller 层通过@SaCheckRole 拦截）
            studentId -> {
                log.warn("学生用户尝试访问公告列表，此处理论上不会执行");
                wrapper.eq(BizNotice::getId, -1L); // 返回空结果
            },
            // 教师：无法查看公告列表（已在 Controller 层通过@SaCheckRole 拦截）
            teacherId -> {
                log.warn("教师用户尝试访问公告列表，此处理论上不会执行");
                wrapper.eq(BizNotice::getId, -1L); // 返回空结果
            },
            // 院系管理员：只能查看自己发布的公告
            departmentAdminDepartmentId -> {
                wrapper.eq(BizNotice::getPublisherId, currentUserId);
                log.info("院系管理员 {} 查询公告列表，仅查看自己发布的公告", currentUserId);
            }
        );
    }

    /**
     * 为前端用户（学生/教师）添加目标范围和院系的双重过滤
     * 确保院系管理员发布的 targetScope=0 的公告只对本学院可见
     *
     * @param wrapper 查询条件
     * @param currentUserType 当前用户类型
     * @param currentDepartmentId 当前用户院系 ID
     */
    /**
     * 根据目标范围和院系双重过滤逻辑添加查询条件
     * 优化后的仪表盘通知公告过滤规则：
     * 
     * 核心判断逻辑：
     * - 通过 publisher_id 关联 sys_user 表获取 user_type 来判断发布者类型
     * - user_type = 'system_admin' → 系统管理员发布
     * - user_type = 'department_admin' → 院系管理员发布
     * 
     * 1. 院系管理员看到：
     *    1.1 系统管理员发布的全体公告（user_type='system_admin', targetScope=0）
     *    1.2 系统管理员发布的院系管理员公告且院系一致（user_type='system_admin', targetScope=3, departmentId=自己的院系）
     * 
     * 2. 教师看到：
     *    2.1 系统管理员发布的全体公告（user_type='system_admin', targetScope=0）
     *    2.2 系统管理员发布的教师公告（无论是否选择院系）
     *        - user_type='system_admin', targetScope=2, departmentId=null → 发给全体教师
     *        - user_type='system_admin', targetScope=2, departmentId!=null → 发给特定院系的教师
     *    2.3 院系管理员发布的本院系全体公告（user_type='department_admin', targetScope=0, departmentId=自己的院系）
     *    2.4 院系管理员发布的本院系教师公告（user_type='department_admin', targetScope=2, departmentId=自己的院系）
     * 
     * 3. 学生看到：
     *    3.1 系统管理员发布的全体公告（user_type='system_admin', targetScope=0）
     *    3.2 系统管理员发布的学生公告（无论是否选择院系）
     *        - user_type='system_admin', targetScope=1, departmentId=null → 发给全体学生
     *        - user_type='system_admin', targetScope=1, departmentId!=null → 发给特定院系的学生
     *    3.3 院系管理员发布的本院系全体公告（user_type='department_admin', targetScope=0, departmentId=自己的院系）
     *    3.4 院系管理员发布的本院系学生公告（user_type='department_admin', targetScope=1, departmentId=自己的院系）
     *
     * @param wrapper 查询条件包装器
     * @param currentUserType 当前用户类型（0-学生，1-教师，2-系统管理员，3-院系管理员）
     * @param currentDepartmentId 当前用户所属院系ID
     */
    private void addTargetScopeAndDepartmentFilter(LambdaQueryWrapper<BizNotice> wrapper, Integer currentUserType, Long currentDepartmentId) {
        if (currentUserType == null) {
            // 未登录用户，只查看全体公告（且没有院系限制的）
            wrapper.and(w -> w
                .eq(BizNotice::getTargetScope, 0) // 全体
            );
            // 排除有院系限制的公告
            wrapper.isNull(BizNotice::getDepartmentId);
        } else if (currentUserType == 0) {
            // 学生：查看系统管理员发布的全体/学生公告 + 院系管理员发布的本院系全体/学生公告
            wrapper.and(w -> {
                // 情况1：系统管理员发布的全体公告（user_type='system_admin', targetScope=0）
                w.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'system_admin'")
                 .eq(BizNotice::getTargetScope, 0)
                 .isNull(BizNotice::getDepartmentId)
                // 情况2：系统管理员发布的学生公告（无论是否有院系限制）
                .or(subW -> subW
                    .exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'system_admin'")
                    .eq(BizNotice::getTargetScope, 1)
                )
                // 情况3：院系管理员发布的本院系全体公告（user_type='department_admin', targetScope=0, departmentId=自己的院系）
                .or(subW -> {
                    if (currentDepartmentId != null) {
                        subW.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'department_admin'")
                             .eq(BizNotice::getTargetScope, 0)
                             .eq(BizNotice::getDepartmentId, currentDepartmentId);
                    }
                })
                // 情况4：院系管理员发布的本院系学生公告（user_type='department_admin', targetScope=1, departmentId=自己的院系）
                .or(subW -> {
                    if (currentDepartmentId != null) {
                        subW.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'department_admin'")
                             .eq(BizNotice::getTargetScope, 1)
                             .eq(BizNotice::getDepartmentId, currentDepartmentId);
                    }
                });
            });
        } else if (currentUserType == 1) {
            // 教师：查看系统管理员发布的全体/教师公告 + 院系管理员发布的本院系全体/教师公告
            wrapper.and(w -> {
                // 情况1：系统管理员发布的全体公告（user_type='system_admin', targetScope=0）
                w.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'system_admin'")
                 .eq(BizNotice::getTargetScope, 0)
                 .isNull(BizNotice::getDepartmentId)
                // 情况2：系统管理员发布的教师公告（无论是否有院系限制）
                .or(subW -> subW
                    .exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'system_admin'")
                    .eq(BizNotice::getTargetScope, 2)
                )
                // 情况3：院系管理员发布的本院系全体公告（user_type='department_admin', targetScope=0, departmentId=自己的院系）
                .or(subW -> {
                    if (currentDepartmentId != null) {
                        subW.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'department_admin'")
                             .eq(BizNotice::getTargetScope, 0)
                             .eq(BizNotice::getDepartmentId, currentDepartmentId);
                    }
                })
                // 情况4：院系管理员发布的本院系教师公告（user_type='department_admin', targetScope=2, departmentId=自己的院系）
                .or(subW -> {
                    if (currentDepartmentId != null) {
                        subW.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'department_admin'")
                             .eq(BizNotice::getTargetScope, 2)
                             .eq(BizNotice::getDepartmentId, currentDepartmentId);
                    }
                });
            });
        } else if (currentUserType == 3) {
            // 院系管理员：查看系统管理员发布的全体公告 + 系统管理员发布的本院系管理员公告
            wrapper.and(w -> {
                // 情况1：系统管理员发布的全体公告（user_type='system_admin', targetScope=0）
                w.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'system_admin'")
                 .eq(BizNotice::getTargetScope, 0)
                 .isNull(BizNotice::getDepartmentId)
                // 情况2：系统管理员发布的院系管理员公告且院系一致（user_type='system_admin', targetScope=3, departmentId=自己的院系）
                .or(subW -> {
                    if (currentDepartmentId != null) {
                        subW.exists("SELECT 1 FROM sys_user u WHERE u.id = biz_notice.publisher_id AND u.user_type = 'system_admin'")
                             .eq(BizNotice::getTargetScope, 3)
                             .eq(BizNotice::getDepartmentId, currentDepartmentId);
                    }
                });
            });
        }
    }

    /**
     * 根据目标范围过滤通知（简化版，用于特殊情况）
     * 注意：此方法已不再在 getNoticePage 中使用，保留用于可能的特殊场景
     *
     * @param notice 通知对象
     * @return 是否符合目标范围
     * @deprecated 已在数据库级别通过 addPermissionFilter 实现权限过滤
     */
    @Deprecated
    private boolean filterByTargetScope(BizNotice notice) {
        // 如果目标范围为 0（全体），则所有人都可以看到
        if (notice.getTargetScope() == null || notice.getTargetScope() == 0) {
            return true;
        }
        
        // 从查询条件中获取当前用户类型
        // 0-学生，1-教师，2-管理员
        Integer currentUserType = dataPermissionUtil.getCurrentUserTypeCode();
        
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
            (notice.getStatus() == NoticeStatus.DRAFT.getCode() || 
             notice.getStatus() == NoticeStatus.WITHDRAWN.getCode())) {
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
        NoticeType type = IEnum.getByCode(NoticeType.class,notice.getType());
        if (type != null) {
            vo.setTypeDesc(type.getDescription());
        }
    
        NoticeStatus status = IEnum.getByCode(NoticeStatus.class,notice.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDescription());
        }
    
        // 填充发布者信息
        if (notice.getPublisherId() != null) {
            SysUser publisher = sysUserMapper.selectById(notice.getPublisherId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getRealName());
                // 查询管理员编号
                BizAdmin admin = bizAdminMapper.selectOne(new LambdaQueryWrapper<BizAdmin>()
                    .eq(BizAdmin::getUserId, notice.getPublisherId()));
                if (admin != null) {
                    vo.setPublisherAdminId(admin.getAdminId());
                }
            }
        }
    
        // 填充院系信息
        if (notice.getDepartmentId() != null) {
            SysDepartment department = sysDepartmentMapper.selectById(notice.getDepartmentId());
            if (department != null) {
                vo.setDepartmentName(department.getName());
                vo.setDepartmentCode(department.getCode());
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
     * 验证目标范围和院系的匹配关系
     * - 系统管理员：可以选择任意目标范围，选择非全体时必须指定院系
     * - 院系管理员：只能选择本院系相关的目标范围，自动关联自己的院系
     *
     * @param createDTO 创建 DTO
     * @param publisherId 发布者 ID
     */
    private void validateTargetScopeAndDepartment(NoticeCreateDTO createDTO, Long publisherId) {
        Integer currentUserType = dataPermissionUtil.getCurrentUserTypeCode();
        Long currentDepartmentId = dataPermissionUtil.getCurrentUserDepartmentId();
        
        if (currentUserType == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "用户类型未知");
        }
        
        // 系统管理员验证
        if (currentUserType == 2) { // system_admin
            // 选择非全体目标范围时，必须指定院系
            if (createDTO.getTargetScope() != null && createDTO.getTargetScope() != 0) {
                if (createDTO.getDepartmentId() == null) {
                    log.info("系统管理员发布目标范围={}，未指定院系，默认为全体", createDTO.getTargetScope());
                    // 不指定院系代表发布给全体
                }
            }
        }
        // 院系管理员验证
        else if (currentUserType == 3) { // department_admin
            if (currentDepartmentId == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系管理员未找到所属院系");
            }
            
            // 强制设置为当前用户的院系 ID
            if (!currentDepartmentId.equals(createDTO.getDepartmentId())) {
                log.warn("院系管理员尝试跨院系发布公告，已自动修正为本院系：departmentId={}", createDTO.getDepartmentId());
            }
            createDTO.setDepartmentId(currentDepartmentId);
            
            // 院系管理员不能发布给全体（跨院系），如果选择全体则改为本院系范围
            if (createDTO.getTargetScope() != null && createDTO.getTargetScope() == 0) {
                log.info("院系管理员发布全体公告，实际范围为本院系，将 targetScope 从 0 调整为保留 0 但通过 departmentId 限制");
                // 注意：这里保持 targetScope=0，但在查询时会通过 departmentId 进行过滤
            }
        }
    }

    /**
     * 验证目标范围和院系的匹配关系（更新时使用）
     */
    private void validateTargetScopeAndDepartment(NoticeUpdateDTO updateDTO, Long updaterId) {
        Integer currentUserType = dataPermissionUtil.getCurrentUserTypeCode();
        Long currentDepartmentId = dataPermissionUtil.getCurrentUserDepartmentId();
        
        if (currentUserType == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "用户类型未知");
        }
        
        // 系统管理员验证
        if (currentUserType == 2) { // system_admin
            // 选择非全体目标范围时，必须指定院系
            if (updateDTO.getTargetScope() != null && updateDTO.getTargetScope() != 0) {
                if (updateDTO.getDepartmentId() == null) {
                    log.info("系统管理员更新目标范围={}，未指定院系，默认为全体", updateDTO.getTargetScope());
                    // 不指定院系代表发布给全体
                }
            }
        }
        // 院系管理员验证
        else if (currentUserType == 3) { // department_admin
            if (currentDepartmentId == null) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系管理员未找到所属院系");
            }
            
            // 强制设置为当前用户的院系 ID
            if (!currentDepartmentId.equals(updateDTO.getDepartmentId())) {
                log.warn("院系管理员尝试跨院系更新公告，已自动修正为本院系：departmentId={}", updateDTO.getDepartmentId());
            }
            updateDTO.setDepartmentId(currentDepartmentId);
        }
    }

    /**
     * 验证生效时间逻辑
     * - 不填写时间代表无期限（永久生效）
     * - 开始时间必须在结束时间之前
     * - 结束时间必须在未来
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void validateEffectiveTime(LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 都不为空时，验证开始时间必须在结束时间之前
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                    "生效开始时间不能晚于结束时间");
            }
        }
        
        // 2. 结束时间必须在未来
        if (endTime != null) {
            if (endTime.isBefore(LocalDateTime.now()) || endTime.isEqual(LocalDateTime.now())) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                    "结束时间必须在未来");
            }
        }
    }

    /**
     * 自动撤回已过期的公告
     * 当公告超过结束时间后，自动将其状态改为已撤回
     *
     * @param notice 通知公告
     * @return 如果公告有效返回 true，已被撤回返回 false
     */
    private boolean autoWithdrawExpiredNotice(BizNotice notice) {
        // 只有已发布的公告才需要检查
        if (notice.getStatus() == null || notice.getStatus() != NoticeStatus.PUBLISHED.getCode()) {
            return true;
        }
        
        // 如果没有设置结束时间，认为不会过期
        if (notice.getEndTime() == null) {
            return true;
        }
        
        // 检查是否已过期
        if (LocalDateTime.now().isAfter(notice.getEndTime())) {
            log.info("公告 {} 已超过结束时间，自动撤回", notice.getId());
            notice.setStatus(NoticeStatus.WITHDRAWN.getCode());
            updateById(notice);
            clearNoticeCache(notice.getId());
            return false;
        }
        
        return true;
    }
}