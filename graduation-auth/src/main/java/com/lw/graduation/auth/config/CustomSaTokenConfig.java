package com.lw.graduation.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义Sa-Token权限验证配置
 * 修改权限验证逻辑，使@SaCheckRole注解基于userType字段进行权限控制
 *
 * @author lw
 */
@Component
@Slf4j
public class CustomSaTokenConfig implements StpInterface {

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
        // 获取用户类型作为权限码
        String userType = getUserTypeFromSession();
        List<String> permissionList = new ArrayList<>();
        
        if (userType != null) {
            permissionList.add(userType);
            log.debug("用户 {} 的权限码: {}", loginId, userType);
        }
        
        return permissionList;
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
        // 获取用户类型作为角色
        String userType = getUserTypeFromSession();
        List<String> roleList = new ArrayList<>();
        
        if (userType != null) {
            roleList.add(userType);
            log.debug("用户 {} 的角色: {}", loginId, userType);
        }
        
        return roleList;
    }

    /**
     * 从Sa-Token Session中获取用户类型
     *
     * @return 用户类型字符串
     */
    private String getUserTypeFromSession() {
        try {
            Object userTypeObj = StpUtil.getTokenSession().get("userType");
            if (userTypeObj != null) {
                return userTypeObj.toString();
            }
        } catch (Exception e) {
            log.warn("从Session获取用户类型失败: {}", e.getMessage());
        }
        return null;
    }
}