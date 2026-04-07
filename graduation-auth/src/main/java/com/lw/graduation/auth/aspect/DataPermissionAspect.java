package com.lw.graduation.auth.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.auth.service.PermissionValidationService;
import com.lw.graduation.common.annotation.DataPermission;
import com.lw.graduation.domain.enums.user.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 数据权限切面
 * 用于拦截带有@DataPermission注解的方法，进行数据权限控制
 *
 * @author lw
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionAspect {

    private final PermissionValidationService permissionValidationService;

    /**
     * 环绕通知，处理数据权限控制
     *
     * @param point 切点
     * @param dataPermission 数据权限注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(dataPermission)")
    public Object doAround(ProceedingJoinPoint point, DataPermission dataPermission) throws Throwable {
        // 如果禁用了数据权限控制，则直接执行原方法
        if (!dataPermission.enabled()) {
            return point.proceed();
        }

        // 获取当前登录用户信息
        Long userId = StpUtil.getLoginIdAsLong();
        // 获取用户角色（需要根据实际的SaToken配置调整）
        String role = getUserRole();

        // 获取方法参数
        Object[] args = point.getArgs();

        // 根据权限类型进行不同的验证
        DataPermission.Type permissionType = dataPermission.value();
        
        switch (permissionType) {
            case SELF:
                // 仅限本人数据访问
                if (!validateSelfAccess(userId, args)) {
                    throw new SecurityException("无权限访问他人数据");
                }
                break;
                
            case TEACHER_STUDENT:
                // 教师查看指导学生数据
                if (!validateTeacherStudentAccess(userId, role, args)) {
                    throw new SecurityException("无权限访问非指导学生数据");
                }
                break;
                
            case DEPARTMENT:
                // 院系内数据访问
                if (!validateDepartmentAccess(userId, role, args)) {
                    throw new SecurityException("无权限访问非本院系数据");
                }
                break;
                
            case ALL:
                // 全部数据访问（默认管理员权限）
                if (!validateAdminAccess(role)) {
                    throw new SecurityException("无权限访问全部数据");
                }
                break;
                
            default:
                log.warn("未知的数据权限类型: {}", permissionType);
                break;
        }

        // 执行原方法
        return point.proceed();
    }

    /**
     * 验证本人数据访问权限
     *
     * @param userId 当前用户ID
     * @param args 方法参数
     * @return 是否有权限
     */
    private boolean validateSelfAccess(Long userId, Object[] args) {
        // 检查参数中是否包含用户ID，验证是否为本人操作
        for (Object arg : args) {
            if (arg instanceof Long && arg.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证教师指导学生数据访问权限
     *
     * @param userId 当前用户ID
     * @param role 用户角色
     * @param args 方法参数
     * @return 是否有权限
     */
    private boolean validateTeacherStudentAccess(Long userId, String role, Object[] args) {
        // 教师角色才能访问指导学生数据
        if (!"teacher".equals(role)) {
            return false;
        }

        // 检查参数中的学生ID是否为当前教师指导的学生
        for (Object arg : args) {
            if (arg instanceof Long studentId) {
                if (permissionValidationService.canAccessStudentData(userId, studentId, role)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 验证院系数据访问权限
     *
     * @param userId 当前用户ID
     * @param role 用户角色
     * @param args 方法参数
     * @return 是否有权限
     */
    private boolean validateDepartmentAccess(Long userId, String role, Object[] args) {
        // 管理员和院系管理员可以访问院系数据
        if ("admin".equals(role) || "department_admin".equals(role)) {
            return true;
        }

        // 教师和学生只能访问本院系数据
        if ("teacher".equals(role) || "student".equals(role)) {
            // 检查参数中的院系ID是否为用户所属院系
            for (Object arg : args) {
                if (arg instanceof Long departmentId) {
                    if (permissionValidationService.canAccessDepartmentData(userId, departmentId, role)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 验证管理员数据访问权限
     *
     * @param role 用户角色
     * @return 是否有权限
     */
    private boolean validateAdminAccess(String role) {
        // 只有系统管理员才能访问全部数据
        return "admin".equals(role);
    }

    /**
     * 获取用户角色
     * 根据 SaToken 的角色配置获取当前登录用户的角色
     * 
     * @return 用户角色
     */
    private String getUserRole() {
        try {
            // 使用 StpUtil.getRoleList() 获取用户的角色列表
            var roleList = StpUtil.getRoleList();
            if (roleList == null || roleList.isEmpty()) {
                return null;
            }
            
            // 按优先级返回第一个匹配的角色
            // system_admin > department_admin > teacher > student
            if (roleList.contains(UserType.SYSTEM_ADMIN.getCode())) {
                return UserType.SYSTEM_ADMIN.getCode();
            } else if (roleList.contains(UserType.DEPARTMENT_ADMIN.getCode())) {
                return UserType.DEPARTMENT_ADMIN.getCode();
            } else if (roleList.contains(UserType.TEACHER.getCode())) {
                return UserType.TEACHER.getCode();
            } else if (roleList.contains(UserType.STUDENT.getCode())) {
                return UserType.STUDENT.getCode();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.warn("获取用户角色失败：{}", e.getMessage());
            return null;
        }
    }
}