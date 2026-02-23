package com.lw.graduation.api.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lw.graduation.domain.entity.role.SysUserRole;

import java.util.List;

/**
 * 用户角色服务接口
 * 提供用户角色关联管理的核心业务逻辑
 *
 * @author lw
 */
public interface UserRoleService extends IService<SysUserRole> {

    /**
     * 获取用户的所有角色
     * 当前项目主要使用的角色查询方法
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查用户是否拥有指定角色编码
     * 当前项目主要使用的角色检查方法
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否拥有该角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 移除用户的所有角色
     * 当前项目主要使用的角色管理方法
     *
     * @param userId 用户ID
     * @return 是否移除成功
     */
    boolean removeAllRoles(Long userId);

    /**
     * 获取用户的所有管理员角色
     * 当前项目主要使用的管理员角色查询方法
     *
     * @param userId 用户ID
     * @return 管理员角色编码列表
     */
    List<String> getUserAdminRoles(Long userId);

    // 以下方法为扩展功能，可根据实际需求逐步启用
    // assignRoles - 为用户分配角色
    // hasRole(Long, SystemRole) - 检查用户是否拥有指定枚举角色
    // getUsersByRole - 获取拥有指定角色的所有用户
    // getUsersByUserType - 获取指定用户类型的所有用户
    // isSystemAdmin - 检查用户是否为系统管理员
    // isDepartmentAdmin - 检查用户是否为院系管理员
    // hasAdminRole - 检查用户是否拥有指定管理员角色
}