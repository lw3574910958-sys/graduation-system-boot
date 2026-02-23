package com.lw.graduation.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.service.user.UserService;
import com.lw.graduation.api.vo.user.UserListInfoVO;
import com.lw.graduation.auth.util.PasswordUtil;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;

import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.user.AccountStatus;
import com.lw.graduation.domain.enums.user.UserType;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 实现用户管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final SysUserMapper sysUserMapper; // 引入用户数据访问接口
    private final PasswordUtil passwordUtil; // 注入密码工具类
    private final CacheHelper cacheHelper; // 注入缓存助手

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件 DTO
     * @return 分页结果
     */
    @Override
    public IPage<UserListInfoVO> getUserPage(UserPageQueryDTO queryDTO){
        // 1. 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getUsername() != null, SysUser::getUsername, queryDTO.getUsername())
                .like(queryDTO.getRealName() != null, SysUser::getRealName, queryDTO.getRealName())
                .eq(queryDTO.getUserType() != null, SysUser::getUserType, queryDTO.getUserType())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .orderByDesc(SysUser::getCreatedAt); // 按创建时间倒序

        // 2. 执行分页查询
        IPage<SysUser> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        // 3. 将实体列表转换为 VO 列表（优化：减少不必要的对象创建）
        IPage<UserListInfoVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToUserListInfoVO) // 转换方法
                .toList());
        voPage.setTotal(userPage.getTotal());

        return voPage;
    }

    /**
     * 根据ID获取用户详情（带缓存穿透防护）
     *
     * @param id 用户ID
     * @return 用户详情 VO
     */
    @Override
    public UserListInfoVO getUserById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.USER_INFO + id;
        
        return cacheHelper.getFromCache(cacheKey, UserListInfoVO.class, () -> {
            SysUser user = sysUserMapper.selectById(id);
            return user != null ? convertToUserListInfoVO(user) : null;
        }, CacheConstants.ExpireTime.USER_INFO_EXPIRE);
    }

    /**
     * 创建新用户
     *
     * @param createDTO 创建用户 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateDTO createDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, createDTO.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResponseCode.USERNAME_EXISTS);
        }

        // 2. 验证用户类型是否有效
        if (!UserType.isValid(createDTO.getUserType())) {
            throw new BusinessException(ResponseCode.USER_TYPE_INVALID);
        }

        // 3. 创建用户实体
        SysUser user = new SysUser();
        user.setUsername(createDTO.getUsername());
        user.setRealName(createDTO.getRealName());

        // 设置默认密码（随机生成）
        String defaultPassword = generateRandomPassword();
        user.setPassword(passwordUtil.encryptPassword(defaultPassword));

        user.setUserType(createDTO.getUserType());
        user.setStatus(createDTO.getStatus() != null ? 
            createDTO.getStatus() : AccountStatus.ENABLED.getValue()); // 默认启用
        user.setLoginFailCount(0);
        user.setLastLoginAt(null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsDeleted(0);

        // 4. 插入数据库
        sysUserMapper.insert(user);

        // 5. 清除可能存在的空值缓存
        clearUserCache(user.getId());
    }

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新用户 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        // 1. 查询用户是否存在
        SysUser existingUser = sysUserMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 检查用户名是否已存在（排除自己）
        // 注意：UserUpdateDTO中没有username字段，所以这里不需要检查用户名唯一性
        // 如果需要支持用户名修改，需要在DTO中添加username字段

        // 3. 构建更新实体
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        // 注意：UserUpdateDTO中没有username字段，所以不更新用户名
        if (updateDTO.getRealName() != null) {
            updateUser.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getUserType() != null) {
            updateUser.setUserType(updateDTO.getUserType());
        }
        if (updateDTO.getStatus() != null) {
            // 验证状态值是否有效
            if (!AccountStatus.isValid(updateDTO.getStatus())) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "无效的账户状态值");
            }
            updateUser.setStatus(updateDTO.getStatus());
        }
        updateUser.setUpdatedAt(LocalDateTime.now());

        // 4. 执行更新
        sysUserMapper.updateById(updateUser);

        // 5. 清除缓存
        clearUserCache(id);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 执行删除（MyBatis-Plus会自动处理逻辑删除，通过@TableLogic注解）
        sysUserMapper.deleteById(id);

        // 3. 清除缓存
        clearUserCache(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long id) {
        updateUserStatus(id, AccountStatus.ENABLED);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long id) {
        updateUserStatus(id, AccountStatus.DISABLED);
    }
    
    /**
     * 更新用户状态的私有方法
     * 
     * @param id 用户ID
     * @param status 目标状态
     */
    private void updateUserStatus(Long id, AccountStatus status) {
        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        
        // 2. 检查当前状态是否与目标状态相同
        AccountStatus currentStatus = AccountStatus.getByValue(user.getStatus());
        if (currentStatus == status) {
            String action = status.isEnabled() ? "启用" : "禁用";
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                String.format("账户已经是%s状态", action));
        }
        
        // 3. 更新状态
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setStatus(status.getValue());
        updateUser.setUpdatedAt(LocalDateTime.now());
        
        sysUserMapper.updateById(updateUser);
        
        // 4. 清除缓存
        clearUserCache(id);
        
        String action = status.isEnabled() ? "启用" : "禁用";
        log.info("用户 {} 账户{}成功，ID: {}", user.getUsername(), action, id);
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id) {
        // 1. 查询用户是否存在
        SysUser existingUser = sysUserMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 生成新密码
        String newPassword = generateRandomPassword();
        String encodedPassword = passwordUtil.encryptPassword(newPassword);

        // 3. 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(encodedPassword);
        updateUser.setUpdatedAt(LocalDateTime.now());

        sysUserMapper.updateById(updateUser);

        // 4. 清除缓存（密码变更）
        clearUserCache(id);

        // 安全日志记录 - 不输出明文密码
        log.info("用户 {} 密码已重置成功，新密码已通过安全渠道发送", existingUser.getUsername());
    }

    /**
     * 将 SysUser 实体转换为 UserListInfoVO 视图对象
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserListInfoVO convertToUserListInfoVO(SysUser user) {
        // 直接返回转换结果，避免冗余的局部变量
        return BeanMapperUtil.copyProperties(user, UserListInfoVO.class);
    }

    /**
     * 统一清除用户缓存
     */
    private void clearUserCache(Long userId) {
        if (userId != null) {
            String cacheKey = CacheConstants.KeyPrefix.USER_INFO + userId;
            cacheHelper.evictCache(cacheKey);
            log.debug("清除用户缓存: {}", cacheKey);
        }
    }

    /**
     * 生成随机密码
     * @return 随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
