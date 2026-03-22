package com.lw.graduation.api.controller.dashboard;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lw.graduation.api.dto.dashboard.AdminDashboardVO;
import com.lw.graduation.api.dto.dashboard.StudentDashboardVO;
import com.lw.graduation.api.dto.dashboard.TeacherDashboardVO;
import com.lw.graduation.api.service.dashboard.DashboardService;
import com.lw.graduation.common.response.Result;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
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
    private final BizStudentMapper bizStudentMapper;
    private final BizTeacherMapper bizTeacherMapper;

    /**
     * 获取学生仪表盘信息
     */
    @GetMapping("/student")
    @Operation(summary = "获取学生仪表盘信息")
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
    public Result<AdminDashboardVO> getAdminDashboard() {
        // 根据用户类型获取院系 ID
        Long departmentId = getCurrentUserDepartmentId();
        AdminDashboardVO vo = dashboardService.getAdminDashboard(departmentId);
        return Result.success(vo);
    }
    
    /**
     * 获取当前用户的院系 ID
     * 系统管理员返回 null，院系管理员返回其所属院系 ID
     * 
     * @return 院系 ID，如果是系统管理员则返回 null
     */
    private Long getCurrentUserDepartmentId() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 尝试从学生表查找
        LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BizStudent::getUserId, userId)
                     .eq(BizStudent::getIsDeleted, 0);
        BizStudent student = bizStudentMapper.selectOne(studentWrapper);
        if (student != null && student.getDepartmentId() != null) {
            return student.getDepartmentId();
        }
        
        // 尝试从教师表查找
        LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(BizTeacher::getUserId, userId)
                     .eq(BizTeacher::getIsDeleted, 0);
        BizTeacher teacher = bizTeacherMapper.selectOne(teacherWrapper);
        if (teacher != null && teacher.getDepartmentId() != null) {
            return teacher.getDepartmentId();
        }
        
        // 如果都没找到，可能是系统管理员
        return null;
    }
}
