package com.lw.graduation.api.controller.department;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.department.DepartmentCreateDTO;
import com.lw.graduation.api.dto.department.DepartmentPageQueryDTO;
import com.lw.graduation.api.dto.department.DepartmentUpdateDTO;
import com.lw.graduation.api.service.department.DepartmentService;
import com.lw.graduation.api.vo.department.DepartmentVO;
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

import java.util.List;

/**
 * 院系管理控制器
 * 提供院系信息的增删改查、分页查询、详情获取等API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/departments")
@Tag(name = "院系管理", description = "院系信息的增删改查、分页查询、详情获取等接口")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 分页查询院系列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询院系列表")
    @SaCheckRole("admin") // 仅管理员可访问
    public Result<IPage<DepartmentVO>> getDepartmentPage(DepartmentPageQueryDTO queryDTO) {
        return Result.success(departmentService.getDepartmentPage(queryDTO));
    }

    /**
     * 根据ID获取院系详情
     *
     * @param id 院系ID
     * @return 院系详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取院系详情")
    @SaCheckRole("admin") // 仅管理员可访问
    public Result<DepartmentVO> getDepartmentById(@PathVariable Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    /**
     * 获取所有院系列表（用于下拉框）
     *
     * @return 院系列表
     */
    @GetMapping
    @Operation(summary = "获取所有院系列表")
    public Result<List<DepartmentVO>> getAllDepartments() {
        return Result.success(departmentService.getAllDepartments());
    }

    /**
     * 创建院系
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建院系")
    @SaCheckRole("admin") // 仅管理员可访问
    public Result<Void> createDepartment(@Validated @RequestBody DepartmentCreateDTO createDTO) {
        departmentService.createDepartment(createDTO);
        return Result.success();
    }

    /**
     * 更新院系
     *
     * @param id 院系ID
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新院系")
    @SaCheckRole("admin") // 仅管理员可访问
    public Result<Void> updateDepartment(@PathVariable Long id, @Validated @RequestBody DepartmentUpdateDTO updateDTO) {
        departmentService.updateDepartment(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除院系
     *
     * @param id 院系ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除院系")
    @SaCheckRole("admin") // 仅管理员可访问
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}