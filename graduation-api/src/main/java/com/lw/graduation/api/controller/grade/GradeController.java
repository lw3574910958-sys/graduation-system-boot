package com.lw.graduation.api.controller.grade;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
 * 需要登录并具有相应权限才能访问。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/grade")
@Tag(name = "成绩管理", description = "成绩信息的增删改查、分页查询、详情获取等接口")
@RequiredArgsConstructor
public class GradeController {

    /**
     * 获取成绩列表
     *
     * @return 成绩列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取成绩列表")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Object> getGradeList() {
        // TODO: 实现成绩列表获取逻辑
        return Result.success();
    }

    /**
     * 根据ID获取成绩详情
     *
     * @param id 成绩ID
     * @return 成绩详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取成绩详情")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Object> getGradeById(@PathVariable Long id) {
        // TODO: 实现成绩详情获取逻辑
        return Result.success();
    }

    /**
     * 创建成绩
     *
     * @param createDTO 创建成绩参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建成绩")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Void> createGrade(@RequestBody Object createDTO) {
        // TODO: 实现成绩创建逻辑
        return Result.success();
    }

    /**
     * 更新成绩信息
     *
     * @param id        成绩ID
     * @param updateDTO 更新成绩参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新成绩信息")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Void> updateGrade(@PathVariable Long id, @RequestBody Object updateDTO) {
        // TODO: 实现成绩更新逻辑
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
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Void> deleteGrade(@PathVariable Long id) {
        // TODO: 实现成绩删除逻辑
        return Result.success();
    }
}