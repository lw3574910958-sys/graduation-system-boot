package com.lw.graduation.auth.util;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.domain.entity.admin.BizAdmin;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.domain.enums.common.IsDepartment;
import com.lw.graduation.infrastructure.mapper.admin.BizAdminMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据权限工具类
 * 提供通用的数据权限判断和过滤功能
 *
 * @author lw
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionUtil {

    private final BizAdminMapper bizAdminMapper;
    private final BizTeacherMapper bizTeacherMapper;
    private final BizStudentMapper bizStudentMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 判断用户是否为院系管理员
     *
     * @param userId 用户 ID
     * @return 是返回 true
     */
    public boolean isDepartmentAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            LambdaQueryWrapper<BizAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizAdmin::getUserId, userId)
                   .eq(BizAdmin::getRoleLevel, IsDepartment.DEPARTMENT.getCode())
                   .eq(BizAdmin::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            return bizAdminMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            log.warn("检查院系管理员身份失败：userId={}", userId, e);
            return false;
        }
    }

    /**
     * 判断当前登录用户是否为院系管理员
     *
     * @return 是返回 true
     */
    public boolean isCurrentLoginUserDepartmentAdmin() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            return isDepartmentAdmin(currentUserId);
        } catch (Exception e) {
            log.warn("检查当前登录用户院系管理员身份失败", e);
            return false;
        }
    }

    /**
     * 根据用户 ID 获取院系 ID（针对院系管理员）
     *
     * @param userId 用户 ID
     * @return 院系 ID
     */
    public Long getDepartmentIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<BizAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizAdmin::getUserId, userId)
                   .eq(BizAdmin::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            BizAdmin admin = bizAdminMapper.selectOne(wrapper);
            return admin != null ? admin.getDepartmentId() : null;
        } catch (Exception e) {
            log.warn("获取用户院系失败：userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取当前登录用户的院系 ID
     *
     * @return 院系 ID
     */
    public Long getCurrentUserDepartmentId() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            return getDepartmentIdByUserId(currentUserId);
        } catch (Exception e) {
            log.warn("获取当前登录用户院系失败", e);
            return null;
        }
    }

    /**
     * 判断用户是否为教师
     *
     * @param userId 用户 ID
     * @return 是返回 true
     */
    public boolean isTeacher(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            LambdaQueryWrapper<BizTeacher> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizTeacher::getUserId, userId)
                   .eq(BizTeacher::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            return bizTeacherMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            log.warn("检查教师身份失败：userId={}", userId, e);
            return false;
        }
    }

    /**
     * 判断当前登录用户是否为教师
     *
     * @return 是返回 true
     */
    public boolean isCurrentLoginUserTeacher() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            return isTeacher(currentUserId);
        } catch (Exception e) {
            log.warn("检查当前登录用户教师身份失败", e);
            return false;
        }
    }

    /**
     * 根据用户 ID 获取教师 ID（针对教师用户）
     *
     * @param userId 用户 ID
     * @return 教师 ID
     */
    public Long getTeacherIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<BizTeacher> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizTeacher::getUserId, userId)
                   .eq(BizTeacher::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            BizTeacher teacher = bizTeacherMapper.selectOne(wrapper);
            return teacher != null ? teacher.getId() : null;
        } catch (Exception e) {
            log.warn("获取教师 ID 失败：userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取当前登录用户的教师 ID
     *
     * @return 教师 ID
     */
    public Long getCurrentUserTeacherId() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            return getTeacherIdByUserId(currentUserId);
        } catch (Exception e) {
            log.warn("获取当前登录用户教师 ID 失败", e);
            return null;
        }
    }

    /**
     * 根据用户 ID 获取学生 ID（针对学生用户）
     *
     * @param userId 用户 ID
     * @return 学生 ID
     */
    public Long getStudentIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizStudent::getUserId, userId)
                   .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            BizStudent student = bizStudentMapper.selectOne(wrapper);
            return student != null ? student.getId() : null;
        } catch (Exception e) {
            log.warn("获取学生 ID 失败：userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取当前登录用户的学生 ID
     *
     * @return 学生 ID
     */
    public Long getCurrentUserStudentId() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            return getStudentIdByUserId(currentUserId);
        } catch (Exception e) {
            log.warn("获取当前登录用户学生 ID 失败", e);
            return null;
        }
    }

    /**
     * 判断用户是否为学生
     *
     * @param userId 用户 ID
     * @return 是返回 true
     */
    public boolean isStudent(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizStudent::getUserId, userId)
                   .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            
            return bizStudentMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            log.warn("检查学生身份失败：userId={}", userId, e);
            return false;
        }
    }

    /**
     * 判断当前登录用户是否为学生
     *
     * @return 是返回 true
     */
    public boolean isCurrentLoginUserStudent() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            return isStudent(currentUserId);
        } catch (Exception e) {
            log.warn("检查当前登录用户学生身份失败", e);
            return false;
        }
    }

    /**
     * 根据用户 ID 获取院系 ID（支持教师和学生）
     *
     * @param userId 用户 ID
     * @return 院系 ID
     */
    public Long getDepartmentIdByUserIdGeneral(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            // 先尝试从管理员表获取（院系管理员）
            Long deptId = getDepartmentIdByUserId(userId);
            if (deptId != null) {
                return deptId;
            }
            
            // 尝试从教师表获取
            LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
            teacherWrapper.eq(BizTeacher::getUserId, userId)
                         .eq(BizTeacher::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
            if (teacher != null && teacher.getDepartmentId() != null) {
                return teacher.getDepartmentId();
            }
            
            // 尝试从学生表获取
            LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(BizStudent::getUserId, userId)
                         .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            BizStudent student = bizStudentMapper.selectOne(studentWrapper);
            if (student != null && student.getDepartmentId() != null) {
                return student.getDepartmentId();
            }
            
            return null;
        } catch (Exception e) {
            log.warn("获取用户院系失败：userId={}", userId, e);
            return null;
        }
    }

    /**
     * 判断两个用户是否属于同一院系（支持教师和学生）
     *
     * @param userId1 用户 ID1
     * @param userId2 用户 ID2
     * @return 同院系返回 true
     */
    public boolean isSameDepartment(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return false;
        }
        
        try {
            Long deptId1 = getDepartmentIdByUserIdGeneral(userId1);
            Long deptId2 = getDepartmentIdByUserIdGeneral(userId2);
            
            if (deptId1 == null || deptId2 == null) {
                return false;
            }
            
            return deptId1.equals(deptId2);
        } catch (Exception e) {
            log.warn("判断同院系失败：userId1={}, userId2={}", userId1, userId2, e);
            return false;
        }
    }

    /**
     * 判断用户是否为指定院系的院系管理员
     *
     * @param userId 用户 ID
     * @param departmentId 院系 ID
     * @return 是返回 true
     */
    public boolean isDepartmentAdminInSpecificDepartment(Long userId, Long departmentId) {
        if (userId == null || departmentId == null) {
            return false;
        }
        
        try {
            if (isDepartmentAdmin(userId)) {
                Long userDeptId = getDepartmentIdByUserId(userId);
                return departmentId != null && departmentId.equals(userDeptId);
            }
            return false;
        } catch (Exception e) {
            log.warn("判断指定院系管理员失败：userId={}, departmentId={}", userId, departmentId, e);
            return false;
        }
    }

    /**
     * 获取当前登录用户的类型编码
     *
     * @return 0-学生，1-教师，2-管理员，null-获取失败
     */
    public Integer getCurrentUserTypeCode() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            if (userId != null) {
                LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(SysUser::getId, userId);
                SysUser user = sysUserMapper.selectOne(wrapper);
                if (user != null && user.getUserType() != null) {
                    String userType = user.getUserType();
                    if ("student".equals(userType)) {
                        return 0;
                    } else if ("teacher".equals(userType)) {
                        return 1;
                    } else if ("admin".equals(userType)) {
                        return 2;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户类型编码失败", e);
        }
        return null;
    }
}
