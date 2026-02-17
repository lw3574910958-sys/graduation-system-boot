package com.lw.graduation.api.controller.grade;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeStatisticsQueryDTO;
import com.lw.graduation.api.service.grade.GradeService;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩管理控制器
 * 提供成绩录入、查询、统计分析等完整的API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/grades")
@Tag(name = "成绩管理", description = "成绩录入、查询、统计分析、自动计算等接口")
@RequiredArgsConstructor
@Slf4j
public class GradeController {

    private final GradeService gradeService;

    /**
     * 分页查询成绩列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询成绩列表")
    public Result<IPage<GradeVO>> getGradePage(GradePageQueryDTO queryDTO) {
        return Result.success(gradeService.getGradePage(queryDTO));
    }

    /**
     * 根据ID获取成绩详情
     *
     * @param id 成绩ID
     * @return 成绩详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取成绩详情")
    public Result<GradeVO> getGradeById(@PathVariable Long id) {
        return Result.success(gradeService.getGradeById(id));
    }

    /**
     * 录入成绩
     *
     * @param inputDTO 录入参数
     * @return 录入结果
     */
    @PostMapping("/input")
    @Operation(summary = "录入成绩")
    @SaCheckRole("teacher")
    public Result<GradeVO> inputGrade(@Validated @RequestBody GradeInputDTO inputDTO) {
        Long graderId = StpUtil.getLoginIdAsLong();
        GradeVO gradeVO = gradeService.inputGrade(inputDTO, graderId);
        return Result.success(gradeVO);
    }

    /**
     * 自动计算综合成绩
     *
     * @param studentId 学生ID
     * @param topicId 题目ID
     * @return 计算结果
     */
    @PostMapping("/calculate")
    @Operation(summary = "自动计算综合成绩")
    @SaCheckRole("teacher")
    public Result<BigDecimal> calculateCompositeGrade(
            @RequestParam Long studentId,
            @RequestParam Long topicId) {
        BigDecimal compositeScore = gradeService.calculateCompositeGrade(studentId, topicId);
        return Result.success(compositeScore);
    }

    /**
     * 删除成绩
     *
     * @param id 成绩ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除成绩")
    @SaCheckRole("teacher")
    public Result<Void> deleteGrade(@PathVariable Long id) {
        Long graderId = StpUtil.getLoginIdAsLong();
        gradeService.deleteGrade(id, graderId);
        return Result.success();
    }

    /**
     * 获取当前教师录入的成绩列表
     *
     * @param queryDTO 查询条件
     * @return 成绩列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取当前教师成绩列表")
    @SaCheckRole("teacher")
    public Result<IPage<GradeVO>> getMyGrades(GradePageQueryDTO queryDTO) {
        Long teacherId = StpUtil.getLoginIdAsLong();
        queryDTO.setGraderId(teacherId);
        return Result.success(gradeService.getGradePage(queryDTO));
    }

    /**
     * 获取某学生的成绩列表
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生成绩列表")
    public Result<List<GradeVO>> getGradesByStudent(@PathVariable Long studentId) {
        return Result.success(gradeService.getGradesByStudent(studentId));
    }

    /**
     * 获取当前学生的所有成绩
     *
     * @return 成绩列表
     */
    @GetMapping("/my/student")
    @Operation(summary = "获取当前学生成绩")
    @SaCheckRole("student")
    public Result<List<GradeVO>> getMyStudentGrades() {
        Long studentId = StpUtil.getLoginIdAsLong();
        return Result.success(gradeService.getGradesByStudent(studentId));
    }

    /**
     * 获取某教师指导学生的成绩列表
     *
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "获取教师指导学生成绩列表")
    public Result<List<GradeVO>> getGradesByTeacher(@PathVariable Long teacherId) {
        return Result.success(gradeService.getGradesByTeacher(teacherId));
    }

    /**
     * 获取当前教师指导学生的成绩列表
     *
     * @return 成绩列表
     */
    @GetMapping("/my/teacher")
    @Operation(summary = "获取当前教师指导学生成绩")
    @SaCheckRole("teacher")
    public Result<List<GradeVO>> getMyTeacherGrades() {
        Long teacherId = StpUtil.getLoginIdAsLong();
        return Result.success(gradeService.getGradesByTeacher(teacherId));
    }

    /**
     * 获取成绩统计信息
     *
     * @param queryDTO 统计查询条件
     * @return 统计结果JSON
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取成绩统计信息")
    @SaCheckRole({"teacher", "admin"})
    public Result<String> getGradeStatistics(GradeStatisticsQueryDTO queryDTO) {
        String statistics = gradeService.getGradeStatistics(queryDTO);
        return Result.success(statistics);
    }

    /**
     * 获取特定题目的成绩分布
     *
     * @param topicId 题目ID
     * @return 成绩分布统计
     */
    @GetMapping("/topic/{topicId}/distribution")
    @Operation(summary = "获取题目成绩分布")
    @SaCheckRole({"teacher", "admin"})
    public Result<String> getTopicGradeDistribution(@PathVariable Long topicId) {
        GradeStatisticsQueryDTO queryDTO = new GradeStatisticsQueryDTO();
        queryDTO.setTopicId(topicId);
        String statistics = gradeService.getGradeStatistics(queryDTO);
        return Result.success(statistics);
    }

    /**
     * 获取特定院系的成绩统计
     *
     * @param departmentId 院系ID
     * @return 成绩统计
     */
    @GetMapping("/department/{departmentId}/statistics")
    @Operation(summary = "获取院系成绩统计")
    @SaCheckRole({"teacher", "admin"})
    public Result<String> getDepartmentGradeStatistics(@PathVariable Long departmentId) {
        GradeStatisticsQueryDTO queryDTO = new GradeStatisticsQueryDTO();
        queryDTO.setDepartmentId(departmentId);
        String statistics = gradeService.getGradeStatistics(queryDTO);
        return Result.success(statistics);
    }
}