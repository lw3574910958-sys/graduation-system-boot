package com.lw.graduation.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.domain.entity.role.SysUserRole;
import com.lw.graduation.domain.enums.permission.AdminRole;
import com.lw.graduation.infrastructure.mapper.role.SysUserRoleMapper;
import com.lw.graduation.api.service.user.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户角色服务实现类
 * 实现用户角色关联管理的核心业务逻辑
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements UserRoleService {

    @Override
    public List<String> getUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getIsDeleted, 0);

        return list(wrapper).stream()
                .map(SysUserRole::getRoleCode)
                .toList();
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleCode, roleCode)
               .eq(SysUserRole::getIsDeleted, 0);

        return count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeAllRoles(Long userId) {
        log.info("移除用户 {} 的所有角色", userId);
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        return remove(wrapper);
    }

    @Override
    public List<String> getUserAdminRoles(Long userId) {
        List<String> allRoles = getUserRoles(userId);

        // 过滤出管理员角色
        return allRoles.stream()
                .filter(roleCode -> AdminRole.getByCode(roleCode) != null)
                .toList();
    }

    // 以下为扩展功能实现，可根据实际需求逐步启用
    // assignRoles - 为用户分配角色
    // hasRole(Long, SystemRole) - 检查用户是否拥有指定枚举角色
    // getUsersByRole - 获取拥有指定角色的所有用户
    // getUsersByUserType - 获取指定用户类型的所有用户
    // isSystemAdmin - 检查用户是否为系统管理员
    // isDepartmentAdmin - 检查用户是否为院系管理员
    // hasAdminRole - 检查用户是否拥有指定管理员角色
}