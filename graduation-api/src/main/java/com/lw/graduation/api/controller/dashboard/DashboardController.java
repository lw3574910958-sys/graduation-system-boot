package com.lw.graduation.api.controller.dashboard;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.api.dto.dashboard.AdminDashboardVO;
import com.lw.graduation.api.dto.dashboard.StudentDashboardVO;
import com.lw.graduation.api.dto.dashboard.TeacherDashboardVO;
import com.lw.graduation.api.service.dashboard.DashboardService;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘管理", description = "仪表盘统计信息管理")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取学生仪表盘信息
     */
    @GetMapping("/student")
    @Operation(summary = "获取学生仪表盘信息")
    @SaCheckRole(value = "student")
    public Result<StudentDashboardVO> getStudentDashboard() {
        Long studentId = StpUtil.getLoginIdAsLong();
        StudentDashboardVO vo = dashboardService.getStudentDashboard(studentId);
        return Result.success(vo);
    }

    /**
     * 获取教师仪表盘信息
     */
    @GetMapping("/teacher")
    @Operation(summary = "获取教师仪表盘信息")
    @SaCheckRole(value = "teacher")
    public Result<TeacherDashboardVO> getTeacherDashboard() {
        Long teacherId = StpUtil.getLoginIdAsLong();
        TeacherDashboardVO vo = dashboardService.getTeacherDashboard(teacherId);
        return Result.success(vo);
    }

    /**
     * 获取管理员仪表盘信息
     */
    @GetMapping("/admin")
    @Operation(summary = "获取管理员仪表盘信息")
    @SaCheckRole(value = {"system_admin", "department_admin"}, mode = SaMode.OR)
    public Result<AdminDashboardVO> getAdminDashboard() {
        Long adminId = StpUtil.getLoginIdAsLong();
        AdminDashboardVO vo = dashboardService.getAdminDashboard(adminId);
        return Result.success(vo);
    }

}
