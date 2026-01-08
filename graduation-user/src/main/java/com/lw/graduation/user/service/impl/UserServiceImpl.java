package com.lw.graduation.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.service.user.UserService;
import com.lw.graduation.api.vo.user.SysUserVO;
import com.lw.graduation.auth.util.PasswordUtil;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.utils.IdUtil;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.UserType;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
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
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper; // 引入用户数据访问接口
    private final PasswordUtil passwordUtil; // 注入密码工具类

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件 DTO
     * @return 分页结果
     */
    @Override
    public IPage<SysUserVO> getUserPage(UserPageQueryDTO queryDTO){
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
        IPage<SysUserVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToVO) // 转换方法
                .toList());
        voPage.setTotal(userPage.getTotal());

        return voPage;
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情 VO
     */
    @Override
    public SysUserVO getUserById(Long id) { // 修改返回类型
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 创建新用户
     *
     * @param createDTO 创建用户 DTO
     */
    @Override
    @Transactional // 开启事务
    public void createUser(UserCreateDTO createDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, createDTO.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResponseCode.USERNAME_EXISTS);
        }

        // 2. 验证用户类型是否有效
        if (!UserType.isValid(createDTO.getUserType())) {
            throw new BusinessException(ResponseCode.USER_TYPE_INVALID.getCode(), "无效的用户类型");
        }

        // 3. 创建用户实体
        SysUser user = new SysUser();
        user.setId(IdUtil.nextId()); // 使用工具类生成ID
        user.setUsername(createDTO.getUsername());
        user.setRealName(createDTO.getRealName());
        user.setUserType(createDTO.getUserType());
        // 使用 PasswordUtil 加密密码
        user.setPassword(passwordUtil.encryptPassword(createDTO.getPassword()));
        user.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 1); // 默认启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 4. 插入数据库
        sysUserMapper.insert(user);
    }

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新用户 DTO
     */
    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        // 1. 查询用户是否存在
        SysUser existingUser = sysUserMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 验证用户类型是否有效 (如果更新了类型)
        if (updateDTO.getUserType() != null && !UserType.isValid(updateDTO.getUserType())) {
            throw new BusinessException(ResponseCode.USER_TYPE_INVALID.getCode(), "无效的用户类型");
        }

        // 3. 构建更新实体
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setRealName(updateDTO.getRealName());
        if (updateDTO.getUserType() != null) {
            updateUser.setUserType(updateDTO.getUserType());
        }
        if (updateDTO.getStatus() != null) {
            updateUser.setStatus(updateDTO.getStatus());
        }
        updateUser.setUpdatedAt(LocalDateTime.now());

        // 4. 执行更新
        sysUserMapper.updateById(updateUser);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        // 2. 执行删除（MyBatis-Plus会自动处理逻辑删除，通过@TableLogic注解）
        sysUserMapper.deleteById(id);
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     */
    @Override
    @Transactional
    public void resetPassword(Long id) {
        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 设置新密码 (使用系统默认密码)
        String defaultPassword = generateRandomPassword(); // 使用随机生成的默认密码
        String encryptedPassword = passwordUtil.encryptPassword(defaultPassword);

        // 3. 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(encryptedPassword);
        updateUser.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(updateUser);
    }

    /**
     * 将 SysUser 实体转换为 SysUserVO 视图对象
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private SysUserVO convertToVO(SysUser user) { // 修改方法返回类型
        SysUserVO vo = new SysUserVO(); // 修改实例化类型
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setUserType(user.getUserType());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        vo.setLastLoginAt(user.getLastLoginAt());
        return vo;
    }

    /**
     * 生成随机密码
     * 
     * @return 随机生成的密码
     */
    private String generateRandomPassword() {
        // 生成包含字母和数字的随机密码
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
