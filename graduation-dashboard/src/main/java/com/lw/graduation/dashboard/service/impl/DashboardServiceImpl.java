package com.lw.graduation.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.api.dto.dashboard.AdminDashboardVO;
import com.lw.graduation.api.dto.dashboard.StudentDashboardVO;
import com.lw.graduation.api.dto.dashboard.TeacherDashboardVO;
import com.lw.graduation.api.service.dashboard.DashboardService;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.enums.status.ReviewStatus;
import com.lw.graduation.domain.enums.status.SelectionStatus;
import com.lw.graduation.domain.enums.status.TopicStatus;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.infrastructure.mapper.document.BizDocumentMapper;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 仪表盘统计服务实现
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final BizStudentMapper bizStudentMapper;
    private final BizTeacherMapper bizTeacherMapper;
    private final BizTopicMapper bizTopicMapper;
    private final BizSelectionMapper bizSelectionMapper;
    private final BizDocumentMapper bizDocumentMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    public StudentDashboardVO getStudentDashboard(Long studentId) {
        log.info("获取学生仪表盘信息，学生 ID: {}", studentId);
        return calculateStudentDashboard(studentId);
    }
    
    /**
     * 计算学生仪表盘数据
     */
    private StudentDashboardVO calculateStudentDashboard(Long studentId) {

        // 1. 查询学生的选题状态
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStudentId, studentId)
                       .eq(BizSelection::getIsDeleted, 0);
        List<BizSelection> selections = bizSelectionMapper.selectList(selectionWrapper);

        // 2. 计算当前流程步骤
        int currentStep = 0; // 0-未选题
        String topicTitle = null;
        Long teacherId = null;

        if (!selections.isEmpty()) {
            // 找到已确认的选题
            BizSelection confirmedSelection = selections.stream()
                .filter(s -> s.getStatus().equals(SelectionStatus.CONFIRMED.getCode()))
                .findFirst()
                .orElse(null);

            if (confirmedSelection != null) {
                currentStep = 1; // 1-已选题
                topicTitle = confirmedSelection.getTopicTitle();
                teacherId = confirmedSelection.getReviewerId();

                // 3. 检查文档提交情况，确定流程步骤
                LambdaQueryWrapper<BizDocument> docWrapper = new LambdaQueryWrapper<>();
                docWrapper.eq(BizDocument::getUserId, studentId)
                         .eq(BizDocument::getReviewStatus, ReviewStatus.APPROVED.getCode());
                List<BizDocument> approvedDocs = bizDocumentMapper.selectList(docWrapper);

                // 根据通过的文档确定步骤
                for (BizDocument doc : approvedDocs) {
                    if (doc.getFileType() == 0) { // 开题报告
                        currentStep = Math.max(currentStep, 2);
                    } else if (doc.getFileType() == 1) { // 中期报告
                        currentStep = Math.max(currentStep, 3);
                    } else if (doc.getFileType() == 2) { // 毕业论文
                        currentStep = Math.max(currentStep, 4);
                    }
                }
            }
        }

        // 4. 统计文档数量
        LambdaQueryWrapper<BizDocument> allDocsWrapper = new LambdaQueryWrapper<>();
        allDocsWrapper.eq(BizDocument::getUserId, studentId)
                     .eq(BizDocument::getIsDeleted, 0);
        List<BizDocument> allDocs = bizDocumentMapper.selectList(allDocsWrapper);

        long pendingDocs = allDocs.stream()
            .filter(d -> d.getReviewStatus().equals(ReviewStatus.PENDING.getCode()))
            .count();
        long submittedDocs = allDocs.size();
        long approvedDocsCount = allDocs.stream()
            .filter(d -> d.getReviewStatus().equals(ReviewStatus.APPROVED.getCode()))
            .count();

        // 5. 获取指导教师姓名
        String teacherName = null;
        if (teacherId != null) {
            LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
            teacherWrapper.eq(BizTeacher::getUserId, teacherId);
            BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
            if (teacher != null) {
                teacherName = teacher.getEmail(); // 这里应该关联 sys_user 获取真实姓名
            }
        }

        return StudentDashboardVO.builder()
            .pendingDocuments((int) pendingDocs)
            .submittedDocuments((int) submittedDocs)
            .approvedDocuments((int) approvedDocsCount)
            .currentStep(currentStep)
            .topicTitle(topicTitle)
            .teacherName(teacherName)
            .totalDocuments((int) submittedDocs)
            .build();
    }
    
    @Override
    public TeacherDashboardVO getTeacherDashboard(Long teacherId) {
        log.info("获取教师仪表盘信息，教师 ID: {}", teacherId);
        return calculateTeacherDashboard(teacherId);
    }
    
    /**
     * 计算教师仪表盘数据
     */
    private TeacherDashboardVO calculateTeacherDashboard(Long teacherId) {
        log.info("获取教师仪表盘信息，教师 ID: {}", teacherId);

        // 1. 统计题目数量
        LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
        topicWrapper.eq(BizTopic::getTeacherId, teacherId)
                   .eq(BizTopic::getIsDeleted, 0);
        List<BizTopic> allTopics = bizTopicMapper.selectList(topicWrapper);

        long totalTopics = allTopics.size();
        long pendingTopics = allTopics.stream()
            .filter(t -> t.getStatus().equals(TopicStatus.REVIEWING.getCode()))
            .count();

        // 2. 统计待审核选题申请
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getReviewerId, teacherId)
                       .eq(BizSelection::getStatus, SelectionStatus.PENDING_REVIEW)
                       .eq(BizSelection::getIsDeleted, 0);
        long pendingSelections = bizSelectionMapper.selectCount(selectionWrapper);

        // 3. 统计待审核文档
        LambdaQueryWrapper<BizDocument> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(BizDocument::getReviewerId, teacherId)
                 .eq(BizDocument::getReviewStatus, ReviewStatus.PENDING.getCode())
                 .eq(BizDocument::getIsDeleted, 0);
        long pendingDocuments = bizDocumentMapper.selectCount(docWrapper);

        // 4. 统计已确认选题数量
        LambdaQueryWrapper<BizSelection> confirmedWrapper = new LambdaQueryWrapper<>();
        confirmedWrapper.eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getCode())
                       .eq(BizSelection::getIsDeleted, 0);
        // 需要关联题目表查询该教师指导的学生
        long confirmedSelections = 0;
        for (BizTopic topic : allTopics) {
            LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizSelection::getTopicId, topic.getId())
                  .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getCode())
                  .eq(BizSelection::getIsDeleted, 0);
            confirmedSelections += bizSelectionMapper.selectCount(wrapper);
        }

        // 5. 统计指导学生总数（通过已确认的选题）
        long totalStudents = confirmedSelections;

        return TeacherDashboardVO.builder()
            .pendingTopics((int) pendingTopics)
            .totalTopics((int) totalTopics)
            .pendingSelections((int) pendingSelections)
            .pendingDocuments((int) pendingDocuments)
            .totalStudents((int) totalStudents)
            .confirmedSelections((int) confirmedSelections)
            .build();
    }
    
    @Override
    public AdminDashboardVO getAdminDashboard(Long departmentId) {
        log.info("获取管理员仪表盘信息，院系 ID: {}", departmentId);
        return calculateAdminDashboard(departmentId);
    }
    
    /**
     * 计算管理员仪表盘数据
     */
    private AdminDashboardVO calculateAdminDashboard(Long departmentId) {
        log.info("获取管理员仪表盘信息，院系 ID: {}", departmentId);

        // 1. 统计待审核题目（本院系或所有院系）
        LambdaQueryWrapper<BizTopic> topicWrapper = new LambdaQueryWrapper<>();
        topicWrapper.eq(BizTopic::getStatus, TopicStatus.REVIEWING.getCode())
                   .eq(BizTopic::getIsDeleted, 0);
        if (departmentId != null) {
            topicWrapper.eq(BizTopic::getDepartmentId, departmentId);
        }
        long pendingTopics = bizTopicMapper.selectCount(topicWrapper);

        // 2. 统计学生和教师数量
        LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BizStudent::getIsDeleted, 0);
        if (departmentId != null) {
            studentWrapper.eq(BizStudent::getDepartmentId, departmentId);
        }
        long totalStudents = bizStudentMapper.selectCount(studentWrapper);

        LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(BizTeacher::getIsDeleted, 0);
        if (departmentId != null) {
            teacherWrapper.eq(BizTeacher::getDepartmentId, departmentId);
        }
        long totalTeachers = bizTeacherMapper.selectCount(teacherWrapper);

        // 3. 统计选题情况
        LambdaQueryWrapper<BizSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getCode())
                       .eq(BizSelection::getIsDeleted, 0);
        long selectedStudents = bizSelectionMapper.selectCount(selectionWrapper);

        long unselectedStudents = totalStudents - selectedStudents;

        // 4. 统计院系数量
        long totalDepartments = 0;
        if (departmentId == null) { // 系统管理员才统计所有院系
            LambdaQueryWrapper<SysDepartment> deptWrapper = new LambdaQueryWrapper<>();
            deptWrapper.eq(SysDepartment::getIsDeleted, 0);
            totalDepartments = sysDepartmentMapper.selectCount(deptWrapper);
        } else {
            totalDepartments = 1;
        }

        // 5. 统计总题目数
        LambdaQueryWrapper<BizTopic> allTopicsWrapper = new LambdaQueryWrapper<>();
        allTopicsWrapper.eq(BizTopic::getIsDeleted, 0);
        if (departmentId != null) {
            allTopicsWrapper.eq(BizTopic::getDepartmentId, departmentId);
        }
        long totalTopics = bizTopicMapper.selectCount(allTopicsWrapper);

        return AdminDashboardVO.builder()
            .pendingTopics((int) pendingTopics)
            .totalStudents((int) totalStudents)
            .totalTeachers((int) totalTeachers)
            .selectedStudents((int) selectedStudents)
            .unselectedStudents((int) unselectedStudents)
            .totalDepartments((int) totalDepartments)
            .totalTopics((int) totalTopics)
            .build();
    }
}
