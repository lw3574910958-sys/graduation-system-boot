package com.lw.graduation.api.vo.topic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课题信息视图对象 (View Object)
 * 用于向外部（如前端）展示课题的详细信息。
 * 对应领域层的 BizTopic 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "课题信息视图对象")
public class TopicVO {

    /**
     * 课题ID
     */
    private Long id;

    /**
     * 课题标题
     */
    @Schema(description = "课题标题")
    private String title;

    /**
     * 课题描述
     */
    @Schema(description = "课题描述")
    private String description;

    /**
     * 指导教师ID
     */
    @Schema(description = "指导教师ID")
    private Long teacherId;

    /**
     * 状态 (1-开放, 2-已选, 3-关闭)
     */
    @Schema(description = "状态 (1-开放, 2-已选, 3-关闭)")
    private Integer status;

    /**
     * 所属院系ID
     */
    @Schema(description = "所属院系ID")
    private Long departmentId;

    /**
     * 题目来源
     */
    @Schema(description = "题目来源")
    private String source;

    /**
     * 题目类型
     */
    @Schema(description = "题目类型")
    private String type;

    /**
     * 题目性质
     */
    @Schema(description = "题目性质")
    private String nature;

    /**
     * 预计难度(1-5)
     */
    @Schema(description = "预计难度(1-5)")
    private Integer difficulty;

    /**
     * 预计工作量(1-5)
     */
    @Schema(description = "预计工作量(1-5)")
    private Integer workload;

    /**
     * 选题人数限制
     */
    @Schema(description = "选题人数限制")
    private Integer maxSelections;

    /**
     * 已选人数
     */
    @Schema(description = "已选人数")
    private Integer selectedCount;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD) // 格式化时间输出
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = CommonConstants.DateTimeFormat.STANDARD)
    private LocalDateTime updatedAt;
}