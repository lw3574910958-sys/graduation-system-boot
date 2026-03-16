package com.lw.graduation.auth.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.vo.auth.CaptchaVO;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.api.vo.user.LoginUserInfoVO;
import com.lw.graduation.auth.util.CaptchaUtil;
import com.lw.graduation.auth.util.PasswordUtil;
import com.lw.graduation.common.config.SaTokenProperties;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.common.util.EnumUtils;
import com.lw.graduation.domain.entity.admin.BizAdmin;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.common.IsDepartment;
import com.lw.graduation.domain.enums.user.AccountStatus;
import com.lw.graduation.domain.enums.user.UserType;
import com.lw.graduation.infrastructure.mapper.admin.BizAdminMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 认证服务实现类
 * 负责处理用户登录、获取用户信息和生成验证码等核心认证业务逻辑。
 * 通过依赖注入使用 SysUserMapper、CaptchaUtil 和 PasswordUtil。
 *
 * @author lw
 */
@Service // 标记为 Spring 服务组件
@RequiredArgsConstructor // Lombok 注解，为所有 final 修饰的字段生成构造函数，实现依赖注入
@Slf4j
public class AuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AuthService {

    private final SysUserMapper sysUserMapper; // 注入用户数据访问层
    private final BizAdminMapper bizAdminMapper;
    private final CaptchaUtil captchaUtil;     // 注入验证码工具类
    private final PasswordUtil passwordUtil;   // 注入密码工具类
    private  final CacheHelper cacheHelper;
    private final SaTokenProperties saTokenProperties;


    /**
     * 更新用户信息的私有辅助方法。
     * 将指定 ID 的用户信息更新为传入的实体对象中的非空字段。
     *
     * @param userId      用户ID
     * @param updateEntity 包含要更新字段的用户实体
     */
    private void updateUser(Long userId, SysUser updateEntity) {
        updateEntity.setId(userId); // 确保更新操作针对正确的用户ID
        sysUserMapper.updateById(updateEntity); // 执行数据库更新
    }


    /**
     * 用户登录方法。
     * 验证验证码、用户名和密码，成功后生成并返回 Token。
     *
     * @param dto 登录参数，包含用户名、密码、验证码Key和验证码
     * @return 生成的登录 Token 字符串
     * @throws BusinessException 如果验证码错误、用户不存在、账户被禁用、密码错误等
     */
    @Override
    @Transactional
    public String login(LoginDTO dto) {
        // 1. 验证验证码
        if (!captchaUtil.validate(dto.getCaptchaKey(), dto.getCaptchaCode())) { // 调用 CaptchaUtil 的验证方法
            throw new BusinessException(ResponseCode.CAPTCHA_ERROR); // 验证失败则抛出业务异常
        }

        // 2. 根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        // 3. 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 4. 检查账户是否被临时锁定（如果设置了锁定时间且当前时间仍在锁定期内）
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResponseCode.ACCOUNT_LOCKED);
        }

        // 6. 检查账户状态是否为启用
        AccountStatus accountStatus = EnumUtils.fromCode(AccountStatus.class, user.getStatus());
        if (accountStatus == AccountStatus.DISABLED) {
            // 即使密码正确，但账户被禁用，仍视为登录失败
            throw new BusinessException(ResponseCode.ACCOUNT_DISABLED);
        }

        // 5. 验证密码是否正确
        // 使用 PasswordUtil 工具类进行密码校验
        if (!passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            // 密码错误，更新登录失败次数
            SysUser updateEntity = new SysUser();
            updateEntity.setLoginFailCount(user.getLoginFailCount() + 1);
            updateUser(user.getId(), updateEntity);
            throw new BusinessException(ResponseCode.PASSWORD_ERROR); // 抛出密码错误异常
        }

        // 7. 密码验证成功，重置登录失败次数并更新最后登录时间
        SysUser updateEntity = new SysUser();
        updateEntity.setLastLoginAt(LocalDateTime.now());
        updateEntity.setLoginFailCount(0); // 重置失败次数
        updateUser(user.getId(), updateEntity);

        // 8. 使用 Sa-Token 进行登录操作
        // Sa-Token 会根据 sa-token.is-concurrent 配置决定是否踢掉旧会话
        StpUtil.login(user.getId()); // 登录，生成 Token

        // 预热当前用户缓存
        warmUpCurrentUserCache(user.getId());

        return StpUtil.getTokenValue(); // 返回生成的 Token
    }

    /**
     * 获取验证码图片的 DataTransferObject 方法。
     * 生成验证码图片并返回 DataTransferObject，包含验证码图片的 Base64 编码和验证码的 Key。
     *
     * @return 包含验证码图片的 Base64 编码和验证码的 Key 的 CaptchaDTO 对象
     * @throws BusinessException 如果生成验证码图片时发生错误
     */
    @Override
    public CaptchaVO generateCaptchaDto() {
        try {
            // 调用 CaptchaUtil 生成验证码DTO
            return captchaUtil.generateCaptchaDto();
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.CREATE_CAPTCHA_ERROR);
        }
    }

    /**
     * 登出方法。
     * 使用 Sa-Token 进行登出操作。
     */
    @Override
    public void logout() {
        Long userId = null;
        try {
            // 先获取当前登录用户ID（必须在 logout 前调用！）
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户ID失败，跳过缓存清理", e);
        }

        // 执行 Sa-Token 登出（会清除 Token 和 Session）
        StpUtil.logout();

        // 清除用户业务缓存
        if (userId != null) {
            clearCurrentUserCache(userId);
        }
    }

    /**
     * 验证验证码方法。
     * 验证用户输入的验证码是否正确。
     *
     * @param captchaKey   验证码的 Key
     * @param captchaCode  用户输入的验证码
     * @return 如果验证码正确，返回 true；否则返回 false
     */
    @Override
    public boolean checkCaptcha(String captchaKey, String captchaCode) {
        return captchaUtil.validate(captchaKey, captchaCode);
    }

    /**
     * 刷新Token方法。
     * 验证当前用户是否已登录，并返回当前用户的Token。
     *
     * @return 当前用户的Token
     * @throws BusinessException 如果用户未登录
     */
    @Override
    public String refreshToken() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED);
        }

        // 获取当前 Token 的剩余活跃时间（单位：秒）
        long remainingActiveTime = StpUtil.getTokenActiveTimeout();

        // 设定刷新阈值：5分钟（300秒）
        int refreshThresholdSeconds = 300;

        // 如果剩余活跃时间 <= 5分钟，则刷新
        if (remainingActiveTime <= refreshThresholdSeconds) {
            int activeTimeout = saTokenProperties.getActiveTimeout(); // 例如 1800 秒
            StpUtil.renewTimeout(activeTimeout);
            log.debug("Token 活跃时间不足（剩余 {}s），已刷新至 {}s", remainingActiveTime, activeTimeout);
        } else {
            log.debug("Token 活跃时间充足（剩余 {}s），无需刷新", remainingActiveTime);
        }

        return StpUtil.getTokenValue();

    }

    /**
     * 获取当前登录用户信息（带缓存支持）
     *
     * @return 当前登录用户信息
     */
    @Override
    public LoginUserInfoVO getCurrentUserSimpleInfo() {
        // 从 Sa-Token 中获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        String cacheKey = CacheConstants.KeyPrefix.CURRENT_USER + userId;

        return cacheHelper.getFromCache(cacheKey, LoginUserInfoVO.class, () -> {
            // 从数据库查询用户信息
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                log.debug("用户不存在: {}", userId);
                return null; // 返回 null，CacheHelper 会处理空值标记
            }
            if (isDepartmentAdmin(user.getId())){
                user.setUserType(UserType.DEPARTMENT_ADMIN.getCode());
            } else {
                user.setUserType(UserType.SYSTEM_ADMIN.getCode());
            }
            return convertToLoginUserInfoVO(user);
        }, CacheConstants.ExpireTime.CURRENT_USER_EXPIRE);
    }

    /**
     * 清除当前用户缓存（用于用户信息变更后调用）
     * 注意：此方法供外部服务调用，当用户信息发生变更时清除缓存
     */
    public void clearCurrentUserCache(Long userId) {
        if (userId != null) {
            String cacheKey = CacheConstants.KeyPrefix.CURRENT_USER + userId;
            log.debug("用户{}登出，清除缓存: {}",userId, cacheKey);
            cacheHelper.evictCache(cacheKey);
        }
    }

    /**
     * 在用户登录成功后预热当前用户缓存
     */
    public void warmUpCurrentUserCache(Long userId) {
        if (userId != null) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null) {
                String cacheKey = CacheConstants.KeyPrefix.CURRENT_USER + userId;
                LoginUserInfoVO userInfo = convertToLoginUserInfoVO(user);
                cacheHelper.putToCache(cacheKey, userInfo, CacheConstants.ExpireTime.CURRENT_USER_EXPIRE);
                log.debug("预热当前用户缓存: {}", cacheKey);
            }
        }
    }

    /**
     * 将 SysUser 实体转换为 LoginUserInfoVO 视图对象
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private LoginUserInfoVO convertToLoginUserInfoVO(SysUser user) {
        return BeanMapperUtil.copyProperties(user, LoginUserInfoVO.class);
    }

    /**
     * 检查用户是否为院系管理员
     *
     * @param userId 用户ID
     * @return true表示是院系管理员，false表示不是
     */
    private boolean isDepartmentAdmin(Long userId) {
        try {
            // 使用MyBatis-Plus的Lambda查询方式检查是否为院系管理员
            LambdaQueryWrapper<BizAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizAdmin::getUserId, userId)
                    .eq(BizAdmin::getRoleLevel, IsDepartment.DEPARTMENT.getCode()); // role_level = 1 表示院系管理员

            return bizAdminMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            log.warn("检查院系管理员身份失败: userId={}", userId, e);
        }
        return false;
    }
}
