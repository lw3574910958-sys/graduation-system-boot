package com.lw.graduation.api.service.dashboard;

import com.lw.graduation.api.dto.dashboard.AdminDashboardVO;
import com.lw.graduation.api.dto.dashboard.StudentDashboardVO;
import com.lw.graduation.api.dto.dashboard.TeacherDashboardVO;

/**
 * 仪表盘统计服务接口
 *
 * @author lw
 */
public interface DashboardService {

    /**
     * 获取学生仪表盘统计信息
     *
     * @param studentId 学生 ID
     * @return 学生仪表盘统计信息
     */
    StudentDashboardVO getStudentDashboard(Long studentId);

    /**
     * 获取教师仪表盘统计信息
     *
     * @param teacherId 教师 ID
     * @return 教师仪表盘统计信息
     */
    TeacherDashboardVO getTeacherDashboard(Long teacherId);

    /**
     * 获取管理员仪表盘统计信息
     *
     * @param departmentId 院系 ID（可选，null 表示系统管理员）
     * @return 管理员仪表盘统计信息
     */
    AdminDashboardVO getAdminDashboard(Long departmentId);
}
