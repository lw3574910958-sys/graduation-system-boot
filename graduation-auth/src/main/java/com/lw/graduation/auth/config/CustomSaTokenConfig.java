package com.lw.graduation.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.hutool.core.convert.Convert.toLong;

/**
 * 自定义Sa-Token权限验证配置
 * 修改权限验证逻辑，使@SaCheckRole注解基于userType字段进行权限控制
 *
 * @author lw
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomSaTokenConfig implements StpInterface {

    private final SysUserMapper sysUserMapper;
    private final DataPermissionUtil dataPermissionUtil;

    /**
     * 返回指定账号id所拥有的权限码集合
     * 在本系统中，我们将userType作为权限码使用
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 权限暂等于角色（可后续扩展）
        return getRoleList(loginId, loginType);
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     * 在本系统中，我们直接使用userType作为角色标识
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = toLong(loginId);
        if (userId == null) return Collections.emptyList();

        String userType = getUserTypeFromDB(userId);
        if (userType == null) return Collections.emptyList();

        List<String> roleList = new ArrayList<>();
        switch (userType) {
            case "admin" -> {
                if (dataPermissionUtil.isDepartmentAdmin(userId)) {
                    roleList.add("department_admin");
                } else{
                    roleList.add("system_admin");
                }
            }
            case "system_admin" -> roleList.add("system_admin");
            case "department_admin" -> roleList.add("department_admin");
            case "teacher" -> {
                roleList.add("teacher");
                if (dataPermissionUtil.isDepartmentAdmin(userId)) {
                    roleList.add("department_admin");
                }
            }
            case "student" -> roleList.add("student");
        }

        log.debug("用户 {} 的角色列表: {}", loginId, roleList);
        return roleList;
    }

    /**
     * 从数据库中获取用户类型
     *
     * @param userId 用户ID
     * @return 用户类型字符串
     */
    private String getUserTypeFromDB(Long userId) {
        try {
            SysUser user = sysUserMapper.selectById(userId);
            return (user != null && user.getUserType() != null) ? user.getUserType() : null;
        } catch (Exception e) {
            log.warn("查询用户类型失败: userId={}", userId, e);
            return null;
        }
    }

}