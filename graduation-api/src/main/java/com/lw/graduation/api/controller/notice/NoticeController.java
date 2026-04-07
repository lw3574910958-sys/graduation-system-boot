package com.lw.graduation.api.controller.notice;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.notice.NoticeCreateDTO;
import com.lw.graduation.api.dto.notice.NoticePageQueryDTO;
import com.lw.graduation.api.dto.notice.NoticeUpdateDTO;
import com.lw.graduation.api.service.notice.NoticeService;
import com.lw.graduation.api.vo.notice.NoticeVO;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知管理控制器
 * 提供通知公告的增删改查、分页查询、详情获取、发布撤回等API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/notices")
@Tag(name = "通知管理", description = "通知公告的增删改查、分页查询、详情获取、发布撤回等接口")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/page")
    @Operation(summary = "分页查询通知列表")
    @SaCheckRole(value = {"system_admin", "department_admin"}, mode = SaMode.OR)
    public Result<IPage<NoticeVO>> getNoticePage(@Valid NoticePageQueryDTO queryDTO) {
        IPage<NoticeVO> pageResult = noticeService.getNoticePage(queryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取通知详情")
    @Parameter(name = "id", description = "通知ID", required = true)
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<NoticeVO> getNoticeById(@PathVariable Long id) {
        NoticeVO noticeVO = noticeService.getNoticeById(id);
        return Result.success(noticeVO);
    }

    @PostMapping
    @Operation(summary = "创建通知")
    @SaCheckRole(value = {"system_admin", "department_admin"}, mode = SaMode.OR)
    public Result<NoticeVO> createNotice(@Valid @RequestBody NoticeCreateDTO createDTO) {
        Long publisherId = StpUtil.getLoginIdAsLong();
        NoticeVO noticeVO = noticeService.createNotice(createDTO, publisherId);
        return Result.success(noticeVO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通知")
    @Parameter(name = "id", description = "通知ID", required = true)
    @SaCheckRole(value = {"system_admin", "department_admin"}, mode = SaMode.OR)
    public Result<Void> updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeUpdateDTO updateDTO) {
        Long updaterId = StpUtil.getLoginIdAsLong();
        noticeService.updateNotice(id, updateDTO, updaterId);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布通知")
    @Parameter(name = "id", description = "通知ID", required = true)
    @SaCheckRole(value = {"system_admin", "department_admin"}, mode = SaMode.OR)
    public Result<Void> publishNotice(@PathVariable Long id) {
        Long publisherId = StpUtil.getLoginIdAsLong();
        noticeService.publishNotice(id, publisherId);
        return Result.success();
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回通知")
    @Parameter(name = "id", description = "通知ID", required = true)
    @SaCheckRole(value = {"system_admin", "department_admin"}, mode = SaMode.OR)
    public Result<Void> withdrawNotice(@PathVariable Long id) {
        Long publisherId = StpUtil.getLoginIdAsLong();
        noticeService.withdrawNotice(id, publisherId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    @Parameter(name = "id", description = "通知ID", required = true)
    @SaCheckRole(value = "system_admin")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        noticeService.deleteNotice(id, userId);
        return Result.success();
    }

    @GetMapping("/sticky")
    @Operation(summary = "获取置顶通知列表")
    @Parameter(name = "targetScope", description = "目标范围: 0-全体, 1-学生, 2-教师, 3-管理员")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<List<NoticeVO>> getStickyNotices(Integer targetScope) {
        List<NoticeVO> stickyNotices = noticeService.getStickyNotices(targetScope);
        return Result.success(stickyNotices);
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新通知列表")
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<List<NoticeVO>> getLatestNotices(
            @Parameter(name = "targetScope", description = "目标范围") Integer targetScope,
            @Parameter(name = "size", description = "数量") Integer size) {
        List<NoticeVO> latestNotices = noticeService.getLatestNotices(targetScope, size);
        return Result.success(latestNotices);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "增加通知阅读次数")
    @Parameter(name = "id", description = "通知ID", required = true)
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public Result<Integer> increaseReadCount(@PathVariable Long id) {
        Integer readCount = noticeService.increaseReadCount(id);
        return Result.success(readCount);
    }

    @GetMapping("/download-attachment")
    @Operation(summary = "下载公告附件")
    @Parameter(name = "attachmentUrl", description = "附件相对路径", required = true)
    @SaCheckRole(value = {"system_admin", "department_admin", "teacher", "student"}, mode = SaMode.OR)
    public ResponseEntity<Resource> downloadAttachment(@RequestParam String attachmentUrl) {
        return noticeService.downloadAttachmentResponse(attachmentUrl);
    }
}