package com.lw.graduation.auth.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.service.auth.AuthService;
import com.lw.graduation.api.vo.auth.UserVO;
import com.lw.graduation.auth.util.CaptchaUtil;
import com.lw.graduation.auth.util.PasswordUtil;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthServiceImpl implements AuthService {

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
            // 为了安全起见，即使用户不存在也要更新失败计数
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 4. 检查账户是否被临时锁定（如果设置了锁定时间且当前时间仍在锁定期内）
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResponseCode.ACCOUNT_DISABLED);
        }

        // 5. 验证密码是否正确
        // 使用 PasswordUtil 工具类进行密码校验
        boolean passwordMatches = passwordUtil.matches(dto.getPassword(), user.getPassword());

        if (!passwordMatches) {
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
     * 获取当前登录用户信息方法。
     * 根据用户ID查询数据库并返回用户视图对象。
     *
     * @param userId 用户ID
     * @return 包含用户基本信息的 UserVO 对象
     * @throws BusinessException 如果用户不存在
     */
    @Override
    public UserVO getCurrentUser(Long userId) {
        // 1. 根据ID查询用户实体
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 将实体对象转换为视图对象 (VO)
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setRole(user.getUserType()); // 角色通常对应用户类型
        userVO.setCreatedAt(user.getCreatedAt());

        return userVO; // 返回用户视图对象
    }

    /**
     * 生成验证码图片方法。
     * 生成验证码图片并写入 HTTP 响应流，同时返回验证码的 Key。
     *
     * @param response HTTP 响应对象，用于写入图片流和设置响应头
     * @return 验证码的唯一标识 Key
     * @throws IOException 如果写入图片流时发生错误
     */
    @Override
    public String generateCaptcha(HttpServletResponse response) throws IOException {
        // 调用 CaptchaUtil 生成验证码图片并写入响应
        return captchaUtil.generateCaptcha(response);
    }
}
