package com.lw.graduation.api.controller.selection;

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
 * 选题管理控制器
 * 提供选题信息的增删改查、分页查询、详情获取等API端点。
 * 需要登录并具有相应权限才能访问。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/selection")
@Tag(name = "选题管理", description = "选题信息的增删改查、分页查询、详情获取等接口")
@RequiredArgsConstructor
public class SelectionController {

    /**
     * 获取选题列表
     *
     * @return 选题列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取选题列表")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Object> getSelectionList() {
        // TODO: 实现选题列表获取逻辑
        return Result.success();
    }

    /**
     * 根据ID获取选题详情
     *
     * @param id 选题ID
     * @return 选题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取选题详情")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Object> getSelectionById(@PathVariable Long id) {
        // TODO: 实现选题详情获取逻辑
        return Result.success();
    }

    /**
     * 创建选题
     *
     * @param createDTO 创建选题参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建选题")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Void> createSelection(@RequestBody Object createDTO) {
        // TODO: 实现选题创建逻辑
        return Result.success();
    }

    /**
     * 更新选题信息
     *
     * @param id        选题ID
     * @param updateDTO 更新选题参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新选题信息")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Void> updateSelection(@PathVariable Long id, @RequestBody Object updateDTO) {
        // TODO: 实现选题更新逻辑
        return Result.success();
    }

    /**
     * 删除选题
     *
     * @param id 选题ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除选题")
    @SaCheckRole("admin") // 仅管理员可访问，可根据业务需要调整
    public Result<Void> deleteSelection(@PathVariable Long id) {
        // TODO: 实现选题删除逻辑
        return Result.success();
    }
}