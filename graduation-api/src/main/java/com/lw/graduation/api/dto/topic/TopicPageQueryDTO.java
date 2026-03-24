package com.lw.graduation.api.dto.topic;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课题分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课题分页查询参数")
public class TopicPageQueryDTO extends BasePageQueryDTO {
    /**
     * 课题标题（模糊查询）
     */
    @Schema(description = "课题标题")
    private String title;

    /**
     * 指导教师 ID
     */
    @Schema(description = "指导教师 ID")
    private Long teacherId;

    /**
     * 状态 (1-开放，2-审核中，3-已选，4-关闭)
     */
    @Schema(description = "状态")
    private Integer status;

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
     * 预计难度 (1-简单，2-适中，3-困难，4-很难，5-极难)
     */
    @Schema(description = "预计难度")
    private Integer difficulty;

    /**
     * 预计工作量 (1-少于 10 学时，2-10-20 学时，3-20-30 学时，4-30-40 学时，5-40 学时以上)
     */
    @Schema(description = "预计工作量")
    private Integer workload;
}
