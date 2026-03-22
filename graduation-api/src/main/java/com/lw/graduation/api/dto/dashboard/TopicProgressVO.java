package com.lw.graduation.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选题进度统计 VO
 *
 * @author lw
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "选题进度统计")
public class TopicProgressVO {

    /**
     * 开放选题数量
     */
    @Schema(description = "开放选题数量")
    private Integer open;

    /**
     * 审核中题目数量
     */
    @Schema(description = "审核中题目数量")
    private Integer reviewing;

    /**
     * 已选课题数量
     */
    @Schema(description = "已选课题数量")
    private Integer selected;

    /**
     * 关闭课题数量
     */
    @Schema(description = "关闭课题数量")
    private Integer closed;

    /**
     * 总题目数
     */
    @Schema(description = "总题目数")
    private Integer total;
}
