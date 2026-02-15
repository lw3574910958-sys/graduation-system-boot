package com.lw.graduation.api.controller.grade;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.grade.GradeCreateDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeUpdateDTO;
import com.lw.graduation.api.service.grade.GradeService;
import com.lw.graduation.api.vo.grade.GradeVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成绩管理控制器
 * 提供成绩信息的增删改查、分页查询、详情获取等API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/grades")
@Tag(name = "成绩管理", description = "成绩信息的增删改查、分页查询、详情获取、统计分析等接口")
@RequiredArgsConstructor
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
     * @param createDTO 创建参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "录入成绩")
    @SaCheckRole("teacher") // 仅教师可录入成绩
    public Result<Void> createGrade(@Validated @RequestBody GradeCreateDTO createDTO) {
        gradeService.createGrade(createDTO);
        return Result.success();
    }

    /**
     * 更新成绩信息
     *
     * @param id 成绩ID
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新成绩信息")
    @SaCheckRole("teacher") // 仅教师可更新成绩
    public Result<Void> updateGrade(@PathVariable Long id, @Validated @RequestBody GradeUpdateDTO updateDTO) {
        gradeService.updateGrade(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除成绩
     *
     * @param id 成绩ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除成绩")
    @SaCheckRole("admin") // 仅管理员可删除成绩
    public Result<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return Result.success();
    }

    /**
     * 获取某学生的成绩列表
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生成绩列表")
    public Result<IPage<GradeVO>> getGradesByStudent(@PathVariable Long studentId, GradePageQueryDTO queryDTO) {
        queryDTO.setStudentId(studentId);
        return Result.success(gradeService.getGradePage(queryDTO));
    }

    /**
     * 获取某课题的成绩列表
     *
     * @param topicId 课题ID
     * @return 成绩列表
     */
    @GetMapping("/topic/{topicId}")
    @Operation(summary = "获取课题成绩列表")
    public Result<IPage<GradeVO>> getGradesByTopic(@PathVariable Long topicId, GradePageQueryDTO queryDTO) {
        queryDTO.setTopicId(topicId);
        return Result.success(gradeService.getGradePage(queryDTO));
    }

    /**
     * 获取某教师评分的成绩列表
     *
     * @param graderId 教师ID
     * @return 成绩列表
     */
    @GetMapping("/grader/{graderId}")
    @Operation(summary = "获取教师评分成绩列表")
    public Result<IPage<GradeVO>> getGradesByGrader(@PathVariable Long graderId, GradePageQueryDTO queryDTO) {
        queryDTO.setGraderId(graderId);
        return Result.success(gradeService.getGradePage(queryDTO));
    }

    /**
     * 获取成绩统计信息
     *
     * @param queryDTO 查询条件
     * @return 统计结果
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取成绩统计信息")
    @SaCheckRole({"teacher", "admin"}) // 教师和管理员可查看统计
    public Result<Object> getGradeStatistics(GradePageQueryDTO queryDTO) {
        // TODO: 实现成绩统计逻辑，如平均分、最高分、最低分、分布情况等
        return Result.success("统计功能待实现");
    }

    /**
     * 导出成绩报表
     *
     * @param queryDTO 查询条件
     * @return 导出结果
     */
    @GetMapping("/export")
    @Operation(summary = "导出成绩报表")
    @SaCheckRole({"teacher", "admin"}) // 教师和管理员可导出报表
    public Result<String> exportGradeReport(GradePageQueryDTO queryDTO) {
        // TODO: 实现成绩报表导出功能
        return Result.success("导出功能待实现");
    }
}