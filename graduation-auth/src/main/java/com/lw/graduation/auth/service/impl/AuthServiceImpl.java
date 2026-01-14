package com.lw.graduation.auth.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.vo.auth.CaptchaVO;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.auth.util.CaptchaUtil;
import com.lw.graduation.auth.util.PasswordUtil;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class AuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AuthService {

    private final SysUserMapper sysUserMapper; // 注入用户数据访问层
    private final CaptchaUtil captchaUtil;     // 注入验证码工具类
    private final PasswordUtil passwordUtil;   // 注入密码工具类

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
     * 验证验证码的私有辅助方法。
     * 从Redis中获取存储的验证码，并与用户输入的验证码进行比对。
     *
     * @param captchaKey   验证码的唯一标识Key
     * @param captchaCode  用户输入的验证码
     * @throws BusinessException 如果验证码不存在或不匹配
     */
    private void validateCaptcha(String captchaKey, String captchaCode) {
        if (!captchaUtil.validate(captchaKey, captchaCode)) { // 调用 CaptchaUtil 的验证方法
            throw new BusinessException(ResponseCode.CAPTCHA_ERROR); // 验证失败则抛出业务异常
        }
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
    public String login(LoginDTO dto) {
        // 1. 验证验证码
        validateCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode());

        // 2. 根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);

        // 3. 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
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

        // 6. 检查账户状态是否为启用（在密码验证成功后再检查）
        if (user.getStatus() != 1) {
            // 即使密码正确，但账户被禁用，仍视为登录失败
            throw new BusinessException(ResponseCode.ACCOUNT_DISABLED);
        }

        // 4. 检查账户是否被临时锁定（如果设置了锁定时间且当前时间仍在锁定期内）
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResponseCode.ACCOUNT_LOCKED);
        }

        // 7. 密码验证成功，重置登录失败次数并更新最后登录时间
        SysUser updateEntity = new SysUser();
        updateEntity.setLastLoginAt(LocalDateTime.now());
        updateEntity.setLoginFailCount(0); // 重置失败次数
        updateUser(user.getId(), updateEntity);

        // 8. 使用 Sa-Token 进行登录操作
        StpUtil.login(user.getId()); // 登录，生成 Token
        // 将用户信息存入 Session，便于后续访问
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("realName", user.getRealName());
        StpUtil.getSession().set("userType", user.getUserType());

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
        // 使用 Sa-Token 进行登出操作
        StpUtil.logout();
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

    @Override
    public String refreshToken() {
        // 验证当前用户是否已登录
        if (StpUtil.isLogin()) {
            // 检查当前token是否即将过期，如果是则刷新它
            // 在Sa-Token中，保持活跃状态可以通过重新获取token来实现
            // 但更常见的是通过设置会话的最后活跃时间
            StpUtil.checkLogin(); // 确保用户仍然处于登录状态
            // 返回当前token
            return StpUtil.getTokenValue();
        }

        // 如果用户未登录，抛出异常
        throw new BusinessException(ResponseCode.UNAUTHORIZED);
    }
}
