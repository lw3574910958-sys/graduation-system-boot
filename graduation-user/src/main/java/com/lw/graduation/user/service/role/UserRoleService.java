package com.lw.graduation.user.service.role;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lw.graduation.domain.entity.role.SysUserRole;
import com.lw.graduation.domain.enums.permission.SystemRole;

import java.util.List;

/**
 * 用户角色服务接口
 * 提供用户角色关联管理的核心业务逻辑
 *
 * @author lw
 */
public interface UserRoleService extends IService<SysUserRole> {

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roles 角色列表
     * @return 是否分配成功
     */
    boolean assignRoles(Long userId, List<SystemRole> roles);

    /**
     * 获取用户的所有角色
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param role 角色枚举
     * @return 是否拥有该角色
     */
    boolean hasRole(Long userId, SystemRole role);

    /**
     * 检查用户是否拥有指定角色编码
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否拥有该角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 移除用户的所有角色
     *
     * @param userId 用户ID
     * @return 是否移除成功
     */
    boolean removeAllRoles(Long userId);

    /**
     * 获取拥有指定角色的所有用户ID
     *
     * @param roleCode 角色编码
     * @return 用户ID列表
     */
    List<Long> getUsersByRole(String roleCode);
    
    /**
     * 获取指定用户类型的所有用户ID
     *
     * @param userType 用户类型
     * @return 该用户类型对应的用户ID列表
     */
    List<Long> getUsersByUserType(String userType);
    
    /**
     * 检查用户是否为系统管理员
     *
     * @param userId 用户ID
     * @return 是系统管理员返回true
     */
    boolean isSystemAdmin(Long userId);
    
    /**
     * 检查用户是否为院系管理员
     *
     * @param userId 用户ID
     * @return 是院系管理员返回true
     */
    boolean isDepartmentAdmin(Long userId);
    
    /**
     * 根据角色编码检查用户是否拥有该管理员角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 拥有该角色返回true
     */
    boolean hasAdminRole(Long userId, String roleCode);
    
    /**
     * 获取用户的所有管理员角色
     *
     * @param userId 用户ID
     * @return 管理员角色编码列表
     */
    List<String> getUserAdminRoles(Long userId);
}