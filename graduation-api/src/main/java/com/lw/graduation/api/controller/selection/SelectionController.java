package com.lw.graduation.api.controller.selection;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.selection.SelectionCreateDTO;
import com.lw.graduation.api.dto.selection.SelectionPageQueryDTO;
import com.lw.graduation.api.dto.selection.SelectionUpdateDTO;
import com.lw.graduation.api.service.selection.SelectionService;
import com.lw.graduation.api.vo.selection.SelectionVO;
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
 * 选题管理控制器
 * 提供选题信息的增删改查、分页查询、详情获取等API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/selections")
@Tag(name = "选题管理", description = "选题信息的增删改查、分页查询、详情获取等接口")
@RequiredArgsConstructor
public class SelectionController {

    private final SelectionService selectionService;

    /**
     * 分页查询选题列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询选题列表")
    public Result<IPage<SelectionVO>> getSelectionPage(SelectionPageQueryDTO queryDTO) {
        return Result.success(selectionService.getSelectionPage(queryDTO));
    }

    /**
     * 根据ID获取选题详情
     *
     * @param id 选题ID
     * @return 选题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取选题详情")
    public Result<SelectionVO> getSelectionById(@PathVariable Long id) {
        return Result.success(selectionService.getSelectionById(id));
    }

    /**
     * 学生选题
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "学生选题")
    @SaCheckRole("student") // 仅学生可选题
    public Result<Void> createSelection(@Validated @RequestBody SelectionCreateDTO createDTO) {
        selectionService.createSelection(createDTO);
        return Result.success();
    }

    /**
     * 更新选题状态（教师确认）
     *
     * @param id 选题ID
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新选题状态")
    @SaCheckRole("teacher") // 仅教师可确认选题
    public Result<Void> updateSelection(@PathVariable Long id, @Validated @RequestBody SelectionUpdateDTO updateDTO) {
        selectionService.updateSelection(id, updateDTO);
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
    @SaCheckRole("student") // 仅学生可删除自己的选题
    public Result<Void> deleteSelection(@PathVariable Long id) {
        selectionService.deleteSelection(id);
        return Result.success();
    }

    /**
     * 获取某学生的选题记录
     *
     * @param studentId 学生ID
     * @return 选题列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生选题记录")
    public Result<IPage<SelectionVO>> getSelectionsByStudent(@PathVariable Long studentId, SelectionPageQueryDTO queryDTO) {
        queryDTO.setStudentId(studentId);
        return Result.success(selectionService.getSelectionPage(queryDTO));
    }

    /**
     * 获取某课题的选题记录
     *
     * @param topicId 课题ID
     * @return 选题列表
     */
    @GetMapping("/topic/{topicId}")
    @Operation(summary = "获取课题选题记录")
    public Result<IPage<SelectionVO>> getSelectionsByTopic(@PathVariable Long topicId, SelectionPageQueryDTO queryDTO) {
        queryDTO.setTopicId(topicId);
        return Result.success(selectionService.getSelectionPage(queryDTO));
    }
}