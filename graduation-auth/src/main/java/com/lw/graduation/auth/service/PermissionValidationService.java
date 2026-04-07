package com.lw.graduation.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.status.SelectionStatus;
import com.lw.graduation.domain.enums.user.UserType;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限验证服务
 * 提供各种业务权限验证功能
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionValidationService {

    private final BizStudentMapper bizStudentMapper;
    private final BizTopicMapper bizTopicMapper;
    private final BizDocumentMapper bizDocumentMapper;
    private final BizSelectionMapper bizSelectionMapper;
    private final SysUserMapper sysUserMapper;
    private final DataPermissionUtil dataPermissionUtil; // 注入数据权限工具

    /**
     * 验证用户是否有权限操作指定学生数据
     *
     * @param userId 当前用户 ID
     * @param studentId 目标学生 ID
     * @param role 用户角色
     * @return true 表示有权限，false 表示无权限
     */
    public boolean canAccessStudentData(Long userId, Long studentId, String role) {
        // 系统管理员可以访问所有学生数据
        if (UserType.SYSTEM_ADMIN.getCode().equals(role)) {
            return true;
        }

        // 院系管理员可以访问本院系学生数据
        if (UserType.DEPARTMENT_ADMIN.getCode().equals(role)) {
            return dataPermissionUtil.isSameDepartment(userId, studentId);
        }

        // 教师可以访问自己指导的学生数据
        if (UserType.TEACHER.getCode().equals(role)) {
            return isTeacherOfStudent(userId, studentId);
        }

        // 学生只能访问自己的数据
        if (UserType.STUDENT.getCode().equals(role)) {
            // 需要将 userId（用户 ID）转换为学生业务 ID 后再比较
            Long studentBizId = dataPermissionUtil.getStudentIdByUserId(userId);
            return studentBizId != null && studentBizId.equals(studentId);
        }

        return false;
    }

    /**
     * 验证用户是否有权限操作指定文档
     *
     * @param userId 当前用户 ID
     * @param documentId 文档 ID
     * @param role 用户角色
     * @return true 表示有权限，false 表示无权限
     */
    public boolean canAccessDocumentData(Long userId, Long documentId, String role) {
        // 系统管理员可以访问所有文档
        if (UserType.SYSTEM_ADMIN.getCode().equals(role)) {
            return true;
        }

        // 院系管理员可以访问本院系文档
        if (UserType.DEPARTMENT_ADMIN.getCode().equals(role)) {
            return isDocumentInDepartment(userId, documentId);
        }

        // 教师可以访问自己指导学生上传的文档
        if (UserType.TEACHER.getCode().equals(role)) {
            return isTeacherStudentDocument(userId, documentId);
        }

        // 学生只能访问自己的文档
        if (UserType.STUDENT.getCode().equals(role)) {
            return isOwnDocument(userId, documentId);
        }

        return false;
    }

    /**
     * 验证用户是否有权限操作指定院系数据
     *
     * @param userId 当前用户 ID
     * @param departmentId 院系 ID
     * @param role 用户角色
     * @return true 表示有权限，false 表示无权限
     */
    public boolean canAccessDepartmentData(Long userId, Long departmentId, String role) {
        // 系统管理员可以访问所有院系数据
        if (UserType.SYSTEM_ADMIN.getCode().equals(role)) {
            return true;
        }

        // 院系管理员只能访问自己所属院系
        if (UserType.DEPARTMENT_ADMIN.getCode().equals(role)) {
            return isUserInDepartment(userId, departmentId);
        }

        // 教师和学生只能查看自己所属院系
        if (UserType.TEACHER.getCode().equals(role) || UserType.STUDENT.getCode().equals(role)) {
            return isUserInDepartment(userId, departmentId);
        }

        return false;
    }

    /**
     * 验证文档下载权限
     * 适用于所有需要验证文档下载/预览权限的场景
     *
     * @param userId 用户 ID
     * @param document 文档实体
     * @throws BusinessException 无权下载时抛出异常
     */
    public void validateDocumentDownloadPermission(Long userId, BizDocument document) {
        if (document == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 文档所有者可以下载
        if (document.getUserId().equals(userId)) {
            return;
        }

        // 指导教师可以下载（需要将 userId 转换为业务教师 ID）
        BizTopic topic = bizTopicMapper.selectById(document.getTopicId());
        if (topic != null) {
            Long teacherBizId = dataPermissionUtil.getTeacherIdByUserId(userId);
            if (teacherBizId != null && teacherBizId.equals(topic.getTeacherId())) {
                return;
            }
        }

        // 系统管理员和院系管理员可以下载
        String userType = getUserTypeByUserId(userId);
        if (UserType.SYSTEM_ADMIN.getCode().equals(userType) ||
            UserType.DEPARTMENT_ADMIN.getCode().equals(userType)) {
            return;
        }

        throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权下载该文档");
    }

    /**
     * 验证文档上传权限
     * 检查学生是否已确认选题
     *
     * @param userId 用户 ID
     * @param topicId 题目 ID
     * @throws BusinessException 无权上传时抛出异常
     */
    public void validateDocumentUploadPermission(Long userId, Long topicId) {
        // 1. 根据用户 ID 查询学生业务 ID（复用 DataPermissionUtil 工具方法）
        Long studentBizId = dataPermissionUtil.getStudentIdByUserId(userId);
        if (studentBizId == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "未找到学生信息");
        }

        // 2. 检查用户是否已确认该题目
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, studentBizId)
                       .eq(BizSelection::getTopicId, topicId)
                       .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getCode());

        if (bizSelectionMapper.selectCount(selectionWrapper) == 0) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "请先确认选题后再上传文档");
        }
    }

    /**
     * 验证成绩录入权限
     * 检查教师是否具有对指定学生和题目的成绩录入权限
     *
     * @param studentId 学生 ID
     * @param topicId 题目 ID
     * @param graderId 评分教师 ID（用户 ID）
     * @throws BusinessException 权限不足时抛出异常
     */
    public void validateGradeInputPermission(Long studentId, Long topicId, Long graderId) {
        // 1. 检查学生是否选择了该题目
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, studentId)
                       .eq(BizSelection::getTopicId, topicId)
                       .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getCode());

        if (bizSelectionMapper.selectCount(selectionWrapper) == 0) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "该学生未选择此题目");
        }

        // 2. 检查题目是否存在
        BizTopic topic = bizTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 3. 将用户 ID 转换为业务教师 ID
        Long teacherBizId = dataPermissionUtil.getTeacherIdByUserId(graderId);
        if (teacherBizId == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "未找到教师信息");
        }

        // 4. 指导教师可以直接评分
        if (topic.getTeacherId().equals(teacherBizId)) {
            log.debug("指导教师 {} 对学生 {} 的题目 {} 进行评分", graderId, studentId, topicId);
            return;  // 早期返回，避免执行后续复杂验证
        }

        // 5. 检查是否为院系管理员
        if (dataPermissionUtil.isDepartmentAdminInSpecificDepartment(graderId, topic.getDepartmentId())) {
            log.debug("院系管理员 {} 对学生 {} 的题目 {} 进行评分", graderId, studentId, topicId);
            return;
        }

        // 6. 如果以上权限都不满足，抛出权限异常
        throw new BusinessException(ResponseCode.FORBIDDEN.getCode(),
                String.format("教师 %d 无权对题目 %d 进行成绩录入", graderId, topicId));
    }

    /**
     * 验证选题审核权限
     * 检查教师是否有权审核指定选题申请
     *
     * @param selectionId 选题申请 ID
     * @param teacherId 审核教师 ID
     * @throws BusinessException 权限不足时抛出异常
     */
    public void validateSelectionReviewPermission(Long selectionId, Long teacherId) {
        // 1. 获取选题申请信息
        BizSelection selection = bizSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题申请不存在");
        }

        // 2. 获取题目信息
        BizTopic topic = bizTopicMapper.selectById(selection.getTopicId());
        if (topic == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "题目不存在");
        }

        // 3. 通过用户 ID 查询业务教师 ID
        Long teacherBizId = dataPermissionUtil.getTeacherIdByUserId(teacherId);
        if (teacherBizId == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "未找到教师信息");
        }

        // 4. 只有指导教师才能审核
        if (!topic.getTeacherId().equals(teacherBizId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权审核该选题申请");
        }
    }

    /**
     * 验证选题确认权限
     * 检查学生是否有权确认指定选题
     *
     * @param selectionId 选题申请 ID
     * @param userId 学生用户 ID
     * @throws BusinessException 权限不足时抛出异常
     */
    public void validateSelectionConfirmPermission(Long selectionId, Long userId) {
        // 1. 根据用户 ID 查询学生业务 ID
        Long studentBizId = dataPermissionUtil.getStudentIdByUserId(userId);
        if (studentBizId == null) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "未找到学生信息");
        }

        // 2. 获取选题信息
        BizSelection selection = bizSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "选题不存在");
        }

        // 3. 只有学生本人才能确认（使用业务学生 ID 进行比较）
        if (!selection.getStudentId().equals(studentBizId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权确认他人选题");
        }
    }

    /**
     * 验证文档删除权限
     * 检查用户是否有权删除指定文档
     *
     * @param userId 用户 ID
     * @param document 文档实体
     * @throws BusinessException 权限不足时抛出异常
     */
    public void validateDocumentDeletePermission(Long userId, BizDocument document) {
        if (document == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 只有文档所有者才能删除
        if (!document.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权删除他人文档");
        }
    }


    // ==================== 辅助方法 ====================

    /**
     * 根据用户 ID 获取用户类型
     */
    private String getUserTypeByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getId, userId);
            SysUser user = sysUserMapper.selectOne(wrapper);
            return user != null ? user.getUserType() : null;
        } catch (Exception e) {
            log.warn("获取用户类型失败：userId={}", userId, e);
            return null;
        }
    }

    /**
     * 判断教师是否是学生的指导教师
     */
    private boolean isTeacherOfStudent(Long teacherId, Long studentId) {
        // teacherId 是用户 ID，需要转换为业务教师 ID
        Long teacherBizId = dataPermissionUtil.getTeacherIdByUserId(teacherId);
        if (teacherBizId == null) {
            return false;
        }

        // 通过选题关系判断师生关系
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, studentId)
                       .eq(BizSelection::getIsDeleted, IsDelete.NOT_DELETED.getCode());

        var selections = bizSelectionMapper.selectList(selectionWrapper);
        for (BizSelection selection : selections) {
            LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
            topicWrapper.eq(BizTopic::getId, selection.getTopicId())
                       .eq(BizTopic::getTeacherId, teacherBizId)  // 使用业务教师 ID
                       .eq(BizTopic::getIsDeleted, IsDelete.NOT_DELETED.getCode());
            BizTopic topic = bizTopicMapper.selectOne(topicWrapper);
            if (topic != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文档是否属于用户所在院系
     */
    private boolean isDocumentInDepartment(Long userId, Long documentId) {
        Long userDepartmentId = dataPermissionUtil.getDepartmentIdByUserId(userId);
        if (userDepartmentId == null) {
            return false;
        }

        BizDocument document = bizDocumentMapper.selectById(documentId);
        if (document == null || document.getUserId() == null) {
            return false;
        }

        // 获取文档上传者的院系 ID（通过 userId 查找）
        Long docOwnerDeptId = dataPermissionUtil.getDepartmentIdByUserId(document.getUserId());
        return userDepartmentId.equals(docOwnerDeptId);
    }

    /**
     * 判断是否是教师指导学生上传的文档
     */
    private boolean isTeacherStudentDocument(Long teacherId, Long documentId) {
        BizDocument document = bizDocumentMapper.selectById(documentId);
        if (document == null || document.getUserId() == null) {
            return false;
        }

        // 通过文档上传者的 userId 找到对应的学生 ID
        LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BizStudent::getUserId, document.getUserId())
                     .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        BizStudent student = bizStudentMapper.selectOne(studentWrapper);

        if (student != null && student.getId() != null) {
            return isTeacherOfStudent(teacherId, student.getId());
        }

        return false;
    }

    /**
     * 判断是否是用户自己的文档
     */
    private boolean isOwnDocument(Long userId, Long documentId) {
        BizDocument document = bizDocumentMapper.selectById(documentId);
        return document != null && userId.equals(document.getUserId());
    }

    /**
     * 判断用户是否属于指定院系
     */
    private boolean isUserInDepartment(Long userId, Long departmentId) {
        Long userDeptId = dataPermissionUtil.getDepartmentIdByUserId(userId);
        return userDeptId != null && userDeptId.equals(departmentId);
    }

}
