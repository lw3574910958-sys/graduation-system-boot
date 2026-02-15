package com.lw.graduation.api.controller.topic;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.topic.TopicPageQueryDTO;
import com.lw.graduation.api.dto.topic.TopicUpdateDTO;
import com.lw.graduation.api.service.topic.TopicService;
import com.lw.graduation.api.vo.topic.TopicVO;
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
 * 课题管理控制器
 * 提供课题信息的增删改查、分页查询、详情获取等API端点。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/topics")
@Tag(name = "课题管理", description = "课题信息的增删改查、分页查询、详情获取等接口")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /**
     * 分页查询课题列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询课题列表")
    public Result<IPage<TopicVO>> getTopicPage(TopicPageQueryDTO queryDTO) {
        return Result.success(topicService.getTopicPage(queryDTO));
    }

    /**
     * 根据ID获取课题详情
     *
     * @param id 课题ID
     * @return 课题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取课题详情")
    public Result<TopicVO> getTopicById(@PathVariable Long id) {
        return Result.success(topicService.getTopicById(id));
    }

    /**
     * 创建课题
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建课题")
    @SaCheckRole("teacher") // 仅教师可创建课题
    public Result<Void> createTopic(@Validated @RequestBody TopicCreateDTO createDTO) {
        topicService.createTopic(createDTO);
        return Result.success();
    }

    /**
     * 更新课题
     *
     * @param id 课题ID
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新课题")
    @SaCheckRole("teacher") // 仅教师可更新课题
    public Result<Void> updateTopic(@PathVariable Long id, @Validated @RequestBody TopicUpdateDTO updateDTO) {
        topicService.updateTopic(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除课题
     *
     * @param id 课题ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除课题")
    @SaCheckRole("teacher") // 仅教师可删除课题
    public Result<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return Result.success();
    }
}