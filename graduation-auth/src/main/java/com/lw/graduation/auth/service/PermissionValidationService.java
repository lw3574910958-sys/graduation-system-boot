package com.lw.graduation.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.auth.util.DataPermissionUtil;
import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.common.enums.IEnum;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.domain.enums.user.UserType;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import com.lw.graduation.infrastructure.mapper.grade.BizGradeMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
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
    private final BizGradeMapper bizGradeMapper;
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
            return userId.equals(studentId);
        }
            
        return false;
    }
    
    /**
     * 验证用户是否有权限操作指定课题
     *
     * @param userId 当前用户 ID
     * @param topicId 课题 ID
     * @param role 用户角色
     * @return true 表示有权限，false 表示无权限
     */
    public boolean canAccessTopicData(Long userId, Long topicId, String role) {
        // 系统管理员可以访问所有课题
        if (UserType.SYSTEM_ADMIN.getCode().equals(role)) {
            return true;
        }
            
        // 院系管理员可以访问本院系课题
        if (UserType.DEPARTMENT_ADMIN.getCode().equals(role)) {
            return isTopicInDepartment(userId, topicId);
        }
            
        // 教师可以访问自己创建的课题
        if (UserType.TEACHER.getCode().equals(role)) {
            return isTeacherTopic(userId, topicId);
        }
            
        // 学生可以查看已发布的课题
        if (UserType.STUDENT.getCode().equals(role)) {
            return isTopicPublished(topicId);
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
     * 验证用户是否有权限操作指定选题
     *
     * @param userId 当前用户 ID
     * @param selectionId 选题 ID
     * @param role 用户角色
     * @return true 表示有权限，false 表示无权限
     */
    public boolean canAccessSelectionData(Long userId, Long selectionId, String role) {
        // 系统管理员可以访问所有选题数据
        if (UserType.SYSTEM_ADMIN.getCode().equals(role)) {
            return true;
        }
            
        // 院系管理员可以访问本院系选题数据
        if (UserType.DEPARTMENT_ADMIN.getCode().equals(role)) {
            return isSelectionInDepartment(userId, selectionId);
        }
            
        // 教师可以访问自己指导学生的选题
        if (UserType.TEACHER.getCode().equals(role)) {
            return isTeacherStudentSelection(userId, selectionId);
        }
            
        // 学生只能访问自己的选题
        if (UserType.STUDENT.getCode().equals(role)) {
            return isOwnSelection(userId, selectionId);
        }
            
        return false;
    }
    
    /**
     * 验证用户是否有权限操作指定成绩数据
     *
     * @param userId 当前用户 ID
     * @param gradeId 成绩 ID
     * @param role 用户角色
     * @return true 表示有权限，false 表示无权限
     */
    public boolean canAccessGradeData(Long userId, Long gradeId, String role) {
        // 系统管理员可以访问所有成绩数据
        if (UserType.SYSTEM_ADMIN.getCode().equals(role)) {
            return true;
        }
            
        // 院系管理员可以访问本院系成绩数据
        if (UserType.DEPARTMENT_ADMIN.getCode().equals(role)) {
            return isGradeInDepartment(userId, gradeId);
        }
            
        // 教师可以访问自己录入的成绩和自己指导学生的成绩
        if (UserType.TEACHER.getCode().equals(role)) {
            return isTeacherGradeOrStudentGrade(userId, gradeId);
        }
            
        // 学生只能访问自己的成绩
        if (UserType.STUDENT.getCode().equals(role)) {
            return isOwnGrade(userId, gradeId);
        }
            
        return false;
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 判断教师是否是学生的指导教师
     */
    private boolean isTeacherOfStudent(Long teacherId, Long studentId) {
        // 通过选题关系判断师生关系
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, studentId)
                       .eq(BizSelection::getIsDeleted, 0);
        
        var selections = bizSelectionMapper.selectList(selectionWrapper);
        for (BizSelection selection : selections) {
            LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
            topicWrapper.eq(BizTopic::getId, selection.getTopicId())
                       .eq(BizTopic::getTeacherId, teacherId)
                       .eq(BizTopic::getIsDeleted, 0);
            BizTopic topic = bizTopicMapper.selectOne(topicWrapper);
            if (topic != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断课题是否属于用户所在院系
     */
    private boolean isTopicInDepartment(Long userId, Long topicId) {
        // 获取用户所属院系 ID
        Long userDepartmentId = dataPermissionUtil.getDepartmentIdByUserId(userId);
        if (userDepartmentId == null) {
            return false;
        }
        
        // 查询课题的院系 ID
        BizTopic topic = bizTopicMapper.selectById(topicId);
        return topic != null && userDepartmentId.equals(topic.getDepartmentId());
    }
    
    /**
     * 判断是否是教师自己创建的课题
     */
    private boolean isTeacherTopic(Long teacherId, Long topicId) {
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getId, topicId)
               .eq(BizTopic::getTeacherId, teacherId)
               .eq(BizTopic::getIsDeleted, 0);
        return bizTopicMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 判断课题是否已发布（开放或审核中）
     */
    private boolean isTopicPublished(Long topicId) {
        BizTopic topic = bizTopicMapper.selectById(topicId);
        if (topic == null || topic.getStatus() == null) {
            return false;
        }
        
        TopicStatus status = IEnum.getByCode(TopicStatus.class,topic.getStatus());
        return status != null && (status == TopicStatus.OPEN || status == TopicStatus.REVIEWING);
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
                     .eq(BizStudent::getIsDeleted, 0);
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
    
    /**
     * 判断选题是否属于用户所在院系
     */
    private boolean isSelectionInDepartment(Long userId, Long selectionId) {
        Long userDepartmentId = dataPermissionUtil.getDepartmentIdByUserId(userId);
        if (userDepartmentId == null) {
            return false;
        }
        
        BizSelection selection = bizSelectionMapper.selectById(selectionId);
        if (selection == null || selection.getTopicId() == null) {
            return false;
        }
        
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getId, selection.getTopicId())
               .eq(BizTopic::getDepartmentId, userDepartmentId)
               .eq(BizTopic::getIsDeleted, 0);
        return bizTopicMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 判断是否是教师指导学生的选题
     */
    private boolean isTeacherStudentSelection(Long teacherId, Long selectionId) {
        BizSelection selection = bizSelectionMapper.selectById(selectionId);
        if (selection == null || selection.getStudentId() == null) {
            return false;
        }
        
        return isTeacherOfStudent(teacherId, selection.getStudentId());
    }
    
    /**
     * 判断是否是用户自己的选题
     */
    private boolean isOwnSelection(Long userId, Long selectionId) {
        BizSelection selection = bizSelectionMapper.selectById(selectionId);
        if (selection == null || selection.getStudentId() == null) {
            return false;
        }
        
        LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizStudent::getId, selection.getStudentId())
               .eq(BizStudent::getUserId, userId)
               .eq(BizStudent::getIsDeleted, 0);
        return bizStudentMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 判断成绩是否属于用户所在院系
     */
    private boolean isGradeInDepartment(Long userId, Long gradeId) {
        Long userDepartmentId = dataPermissionUtil.getDepartmentIdByUserId(userId);
        if (userDepartmentId == null) {
            return false;
        }
        
        BizGrade grade = bizGradeMapper.selectById(gradeId);
        if (grade == null || grade.getTopicId() == null) {
            return false;
        }
        
        // 通过课题判断是否属于同一院系
        LambdaQueryWrapper<BizTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizTopic::getId, grade.getTopicId())
               .eq(BizTopic::getDepartmentId, userDepartmentId)
               .eq(BizTopic::getIsDeleted, 0);
        return bizTopicMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 判断是否是教师的成绩或指导学生的成绩
     */
    private boolean isTeacherGradeOrStudentGrade(Long teacherId, Long gradeId) {
        // 检查是否是教师录入的成绩（通过 grader_id）
        BizGrade grade = bizGradeMapper.selectById(gradeId);
        if (grade == null) {
            return false;
        }
        
        // 如果是教师录入的成绩
        if (grade.getGraderId() != null && teacherId.equals(grade.getGraderId())) {
            return true;
        }
        
        // 检查是否是指导学生的成绩（通过课题关联）
        if (grade.getTopicId() == null) {
            return false;
        }
        
        LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
        topicWrapper.eq(BizTopic::getId, grade.getTopicId())
                   .eq(BizTopic::getTeacherId, teacherId)
                   .eq(BizTopic::getIsDeleted, 0);
        return bizTopicMapper.selectCount(topicWrapper) > 0;
    }
    
    /**
     * 判断是否是用户自己的成绩
     */
    private boolean isOwnGrade(Long userId, Long gradeId) {
        BizGrade grade = bizGradeMapper.selectById(gradeId);
        if (grade == null || grade.getStudentId() == null) {
            return false;
        }
        
        // 通过成绩的 student_id 找到对应的用户 ID
        LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizStudent::getId, grade.getStudentId())
               .eq(BizStudent::getUserId, userId)
               .eq(BizStudent::getIsDeleted, 0);
        return bizStudentMapper.selectCount(wrapper) > 0;
    }
    
}
