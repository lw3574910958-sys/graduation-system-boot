package com.lw.graduation.api.controller.selection;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.selection.SelectionApplyDTO;
import com.lw.graduation.api.dto.selection.SelectionPageQueryDTO;
import com.lw.graduation.api.dto.selection.SelectionReviewDTO;
import com.lw.graduation.api.service.selection.SelectionService;
import com.lw.graduation.api.vo.selection.SelectionVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 选题管理控制器
 * 提供选题申请、审核、确认等完整的业务流程API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/selections")
@Tag(name = "选题管理", description = "选题申请、审核、确认、查询等接口")
@RequiredArgsConstructor
@Slf4j
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
     * 学生申请选题
     *
     * @param applyDTO 申请参数
     * @return 申请结果
     */
    @PostMapping("/apply")
    @Operation(summary = "学生申请选题")
    @SaCheckRole("student")
    public Result<SelectionVO> applySelection(@Validated @RequestBody SelectionApplyDTO applyDTO) {
        Long studentId = StpUtil.getLoginIdAsLong();
        SelectionVO selectionVO = selectionService.applySelection(applyDTO, studentId);
        return Result.success(selectionVO);
    }

    /**
     * 教师审核选题申请
     *
     * @param reviewDTO 审核参数
     * @return 审核结果
     */
    @PostMapping("/review")
    @Operation(summary = "教师审核选题申请")
    @SaCheckRole("teacher")
    public Result<SelectionVO> reviewSelection(@Validated @RequestBody SelectionReviewDTO reviewDTO) {
        Long teacherId = StpUtil.getLoginIdAsLong();
        SelectionVO selectionVO = selectionService.reviewSelection(reviewDTO, teacherId);
        return Result.success(selectionVO);
    }

    /**
     * 学生确认选题
     *
     * @param id 选题ID
     * @return 确认结果
     */
    @PostMapping("/{id}/confirm")
    @Operation(summary = "学生确认选题")
    @SaCheckRole("student")
    public Result<SelectionVO> confirmSelection(@PathVariable Long id) {
        Long studentId = StpUtil.getLoginIdAsLong();
        SelectionVO selectionVO = selectionService.confirmSelection(id, studentId);
        return Result.success(selectionVO);
    }

    /**
     * 学生撤销选题申请
     *
     * @param id 选题ID
     * @return 撤销结果
     */
    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "学生撤销选题申请")
    @SaCheckRole("student")
    public Result<Void> cancelSelection(@PathVariable Long id) {
        Long studentId = StpUtil.getLoginIdAsLong();
        selectionService.cancelSelection(id, studentId);
        return Result.success();
    }

    /**
     * 删除选题记录
     *
     * @param id 选题ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除选题记录")
    public Result<Void> deleteSelection(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        selectionService.deleteSelection(id, userId);
        return Result.success();
    }

    /**
     * 获取当前学生的选题申请列表
     *
     * @return 选题列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取当前学生选题申请列表")
    @SaCheckRole("student")
    public Result<List<SelectionVO>> getMySelections() {
        Long studentId = StpUtil.getLoginIdAsLong();
        return Result.success(selectionService.getSelectionsByStudent(studentId));
    }

    /**
     * 获取某学生的选题申请列表
     *
     * @param studentId 学生ID
     * @return 选题列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生选题申请列表")
    public Result<List<SelectionVO>> getSelectionsByStudent(@PathVariable Long studentId) {
        return Result.success(selectionService.getSelectionsByStudent(studentId));
    }

    /**
     * 获取当前教师需要审核的选题申请列表
     *
     * @return 选题列表
     */
    @GetMapping("/for-review")
    @Operation(summary = "获取当前教师待审核选题列表")
    @SaCheckRole("teacher")
    public Result<List<SelectionVO>> getSelectionsForReview() {
        Long teacherId = StpUtil.getLoginIdAsLong();
        return Result.success(selectionService.getSelectionsForReview(teacherId));
    }

    /**
     * 获取某教师需要审核的选题申请列表
     *
     * @param teacherId 教师ID
     * @return 选题列表
     */
    @GetMapping("/teacher/{teacherId}/for-review")
    @Operation(summary = "获取教师待审核选题列表")
    public Result<List<SelectionVO>> getSelectionsForReview(@PathVariable Long teacherId) {
        return Result.success(selectionService.getSelectionsForReview(teacherId));
    }

    /**
     * 获取某题目的选题申请列表
     *
     * @param topicId 题目ID
     * @param queryDTO 查询条件
     * @return 选题列表
     */
    @GetMapping("/topic/{topicId}")
    @Operation(summary = "获取题目选题申请列表")
    public Result<IPage<SelectionVO>> getSelectionsByTopic(
            @PathVariable Long topicId, 
            SelectionPageQueryDTO queryDTO) {
        queryDTO.setTopicId(topicId);
        return Result.success(selectionService.getSelectionPage(queryDTO));
    }

    /**
     * 获取某种状态的选题申请列表
     *
     * @param status 状态
     * @param queryDTO 查询条件
     * @return 选题列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "获取指定状态选题列表")
    public Result<IPage<SelectionVO>> getSelectionsByStatus(
            @PathVariable Integer status,
            SelectionPageQueryDTO queryDTO) {
        queryDTO.setStatus(status);
        return Result.success(selectionService.getSelectionPage(queryDTO));
    }
}