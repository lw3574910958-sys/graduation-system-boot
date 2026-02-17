package com.lw.graduation.user.service.role.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.domain.entity.role.SysUserRole;
import com.lw.graduation.domain.enums.SystemRole;
import com.lw.graduation.infrastructure.mapper.role.SysUserRoleMapper;
import com.lw.graduation.user.service.role.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<SystemRole> roles) {
        log.info("为用户 {} 分配角色: {}", userId, roles != null ? 
            roles.stream().map(SystemRole::getCode).collect(Collectors.toList()) : "空列表");
        
        // 先删除用户的所有现有角色
        removeAllRoles(userId);
        
        // 批量插入新角色
        if (roles != null && !roles.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            for (SystemRole role : roles) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleCode(role.getCode());
                userRole.setCreatedAt(now);
                userRole.setUpdatedAt(now);
                userRole.setIsDeleted(0);
                userRoles.add(userRole);
            }
            
            return saveBatch(userRoles);
        }
        
        return true;
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getIsDeleted, 0);
        
        return list(wrapper).stream()
                .map(SysUserRole::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasRole(Long userId, SystemRole role) {
        return hasRole(userId, role.getCode());
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
    public List<Long> getUsersByRole(String roleCode) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleCode, roleCode)
               .eq(SysUserRole::getIsDeleted, 0);
        
        return list(wrapper).stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());
    }
}