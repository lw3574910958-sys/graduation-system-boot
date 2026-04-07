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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
     * 根据用户 ID 获取院系 ID（通用方法，支持管理员、教师、学生）
     * 依次从以下表中获取：
     * 1. 管理员表（院系管理员）
     * 2. 教师表
     * 3. 学生表
     *
     * @param userId 用户 ID
     * @return 院系 ID
     */
    public Long getDepartmentIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 先尝试从管理员表获取（院系管理员）
            LambdaQueryWrapper<BizAdmin> adminWrapper = new LambdaQueryWrapper<>();
            adminWrapper.eq(BizAdmin::getUserId, userId)
                       .eq(BizAdmin::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            BizAdmin admin = bizAdminMapper.selectOne(adminWrapper);
            if (admin != null && admin.getDepartmentId() != null) {
                return admin.getDepartmentId();
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
            Long studentBizId = student != null ? student.getId() : null;
            log.debug("获取学生 ID: userId={}, studentBizId={}", userId, studentBizId);
            return studentBizId;
        } catch (Exception e) {
            log.warn("获取学生 ID 失败：userId={}", userId, e);
            return null;
        }
    }

    /**
     * 根据用户 ID 获取学生对象（针对学生用户）
     *
     * @param userId 用户 ID
     * @return 学生对象
     */
    public BizStudent getStudentByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizStudent::getUserId, userId)
                   .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());

            BizStudent student = bizStudentMapper.selectOne(wrapper);
            log.debug("获取学生对象：userId={}, studentId={}", userId, student != null ? student.getStudentId() : null);
            return student;
        } catch (Exception e) {
            log.warn("获取学生对象失败：userId={}", userId, e);
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
     * 判断两个用户是否属于同一院系（通用方法，支持所有用户）
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
            Long deptId1 = getDepartmentIdByUserId(userId1);
            Long deptId2 = getDepartmentIdByUserId(userId2);

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
     * @return 0-学生，1-教师，2-系统管理员，3-院系管理员，null-获取失败
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
                    // 按照 UserType 枚举映射：0-学生，1-教师，2-系统管理员，3-院系管理员
                    if ("student".equals(userType)) {
                        return 0;
                    } else if ("teacher".equals(userType)) {
                        return 1;
                    } else if ("system_admin".equals(userType)) {
                        return 2;
                    } else if ("department_admin".equals(userType)) {
                        return 3;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户类型编码失败", e);
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户类型字符串（从 Session 中获取）
     * 注意：此方法依赖于登录时 Session 中存储的 userType 信息
     *
     * @return 用户类型字符串：student/teacher/department_admin/system_admin，null-获取失败
     */
    public String getCurrentUserTypeString() {
        try {
            return StpUtil.getSession().get("userType") != null
                ? StpUtil.getSession().get("userType").toString() : null;
        } catch (Exception e) {
            log.warn("获取当前用户类型字符串失败", e);
            return null;
        }
    }

    /**
     * 为分页查询添加通用的数据权限过滤条件
     * 适用于所有需要根据用户角色过滤数据的场景
     *
     * @param wrapper 查询条件包装器
     * @param studentFilter 学生角色的过滤条件（接收 studentId）
     * @param teacherFilter 教师角色的过滤条件（接收 teacherId）
     * @param departmentAdminFilter 院系管理员角色的过滤条件（接收 departmentId）
     * @return 是否成功添加过滤条件（false 表示无权限或获取用户信息失败）
     */
    public boolean addCommonDataPermissionFilter(
            LambdaQueryWrapper<?> wrapper,
            Consumer<Long> studentFilter,
            Consumer<Long> teacherFilter,
            Consumer<Long> departmentAdminFilter
    ) {
        try {
            // 院系管理员
            if (isCurrentLoginUserDepartmentAdmin()) {
                Long departmentId = getCurrentUserDepartmentId();
                if (departmentId != null && departmentAdminFilter != null) {
                    departmentAdminFilter.accept(departmentId);
                    log.info("院系管理员数据权限过滤：departmentId={}", departmentId);
                    return true;
                }
            }
            // 教师
            else if (isCurrentLoginUserTeacher()) {
                Long teacherId = getCurrentUserTeacherId();
                if (teacherId != null && teacherFilter != null) {
                    teacherFilter.accept(teacherId);
                    log.info("教师数据权限过滤：teacherId={}", teacherId);
                    return true;
                }
            }
            // 学生
            else if (isCurrentLoginUserStudent()) {
                Long studentId = getCurrentUserStudentId();
                log.info("学生角色权限过滤：userType={}, studentId={}", getCurrentUserTypeString(), studentId);
                if (studentId != null && studentFilter != null) {
                    studentFilter.accept(studentId);
                    log.info("学生数据权限过滤成功：studentId={}", studentId);
                    return true;
                } else {
                    log.warn("学生数据权限过滤失败：studentId={}", studentId);
                }
            }
            // 系统管理员或其他角色：不需要过滤
            else {
                log.debug("系统管理员或其他角色，无需数据权限过滤");
                return true;
            }
        } catch (Exception e) {
            log.warn("添加通用数据权限过滤失败", e);
        }
        return false;
    }

    /**
     * 为分页查询添加通用的数据权限过滤条件（支持 user_id 字段）
     * 适用于文档表等直接使用 sys_user.id 的场景
     *
     * @param wrapper 查询条件包装器
     * @param userIdColumn 用户 ID 列名（如 "user_id"）
     * @return 是否成功添加过滤条件（false 表示无权限或获取用户信息失败）
     */
    public boolean addCommonDataPermissionFilterForUserIdColumn(
            LambdaQueryWrapper<?> wrapper,
            String userIdColumn
    ) {
        try {
            // 院系管理员 - 无需过滤（通过 topic_id 间接控制）
            if (isCurrentLoginUserDepartmentAdmin()) {
                log.debug("院系管理员角色，通过 topic_id 间接控制权限");
                return true;
            }
            // 教师 - 无需过滤（通过 topic_id 间接控制）
            else if (isCurrentLoginUserTeacher()) {
                log.debug("教师角色，通过 topic_id 间接控制权限");
                return true;
            }
            // 学生 - 只能查看自己的数据
            else if (isCurrentLoginUserStudent()) {
                Long currentUserId = StpUtil.getLoginIdAsLong();
                if (currentUserId != null) {
                    // 使用 apply 方法直接拼接 SQL
                    wrapper.apply(userIdColumn + " = {0}", currentUserId);
                    log.info("学生角色数据权限过滤（user_id 列）：userId={}", currentUserId);
                    return true;
                } else {
                    log.warn("获取当前登录用户 ID 失败");
                }
            }
            // 系统管理员或其他角色：不需要过滤
            else {
                log.debug("系统管理员或其他角色，无需数据权限过滤");
                return true;
            }
        } catch (Exception e) {
            log.warn("添加 user_id 列数据权限过滤失败", e);
        }
        return false;
    }

    /**
     * 根据学生姓名模糊查询学生业务 ID 列表（biz_student.id）
     * 用于跨表关联查询场景
     *
     * @param studentName 学生姓名（支持模糊匹配）
     * @return 学生业务 ID 列表（biz_student.id）
     */
    public List<Long> findStudentIdsByName(String studentName) {
        if (studentName == null || studentName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 先通过用户表查询姓名匹配的学生用户
            LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(SysUser::getRealName, studentName)
                       .eq(SysUser::getUserType, "student"); // 只查询学生类型
            
            List<SysUser> matchedUsers = sysUserMapper.selectList(userWrapper);
            if (matchedUsers.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 获取这些用户的ID
            List<Long> userIds = matchedUsers.stream()
                    .map(SysUser::getId)
                    .toList();
            
            // 通过学生表查询对应的 biz_student.id
            LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.in(BizStudent::getUserId, userIds);
            
            List<BizStudent> students = bizStudentMapper.selectList(studentWrapper);
            // 返回 biz_student.id（不是 user_id）
            return students.stream()
                    .map(BizStudent::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("根据学生姓名查询学生业务 ID 失败：studentName={}", studentName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据学生学号模糊查询学生业务 ID 列表（biz_student.id）
     * 用于跨表关联查询场景
     *
     * @param studentNumber 学生学号（支持模糊匹配）
     * @return 学生业务 ID 列表（biz_student.id）
     */
    public List<Long> findStudentIdsByNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(BizStudent::getStudentId, studentNumber);
            
            List<BizStudent> students = bizStudentMapper.selectList(wrapper);
            // 返回 biz_student.id（不是 user_id）
            return students.stream()
                    .map(BizStudent::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("根据学生学号查询学生业务 ID 失败：studentNumber={}", studentNumber, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据教师姓名模糊查询教师用户 ID 列表
     * 用于跨表关联查询场景
     *
     * @param teacherName 教师姓名（支持模糊匹配）
     * @return 教师用户 ID 列表
     */
    public List<Long> findTeacherUserIdsByName(String teacherName) {
        if (teacherName == null || teacherName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(SysUser::getRealName, teacherName)
                   .eq(SysUser::getUserType, "teacher"); // 只查询教师类型
            
            List<SysUser> users = sysUserMapper.selectList(wrapper);
            return users.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("根据教师姓名查询用户 ID 失败：teacherName={}", teacherName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据教师工号模糊查询教师用户 ID 列表
     * 用于跨表关联查询场景
     *
     * @param workNumber 教师工号（支持模糊匹配）
     * @return 教师用户 ID 列表
     */
    public List<Long> findTeacherUserIdsByWorkNumber(String workNumber) {
        if (workNumber == null || workNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            LambdaQueryWrapper<BizTeacher> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(BizTeacher::getTeacherId, workNumber);
            
            List<BizTeacher> teachers = bizTeacherMapper.selectList(wrapper);
            return teachers.stream()
                    .map(BizTeacher::getUserId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("根据教师工号查询用户 ID 失败：workNumber={}", workNumber, e);
            return new ArrayList<>();
        }
    }
}
