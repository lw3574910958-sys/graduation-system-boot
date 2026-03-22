package com.lw.graduation.api.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成绩分布统计 VO
 *
 * @author lw
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成绩分布统计")
public class GradeDistributionVO {

    /**
     * 优秀人数 (90-100)
     */
    @Schema(description = "优秀人数")
    private Integer excellent;

    /**
     * 良好人数 (80-89)
     */
    @Schema(description = "良好人数")
    private Integer good;

    /**
     * 中等人数 (70-79)
     */
    @Schema(description = "中等人数")
    private Integer medium;

    /**
     * 及格人数 (60-69)
     */
    @Schema(description = "及格人数")
    private Integer pass;

    /**
     * 不及格人数 (<60)
     */
    @Schema(description = "不及格人数")
    private Integer fail;

    /**
     * 总人数
     */
    @Schema(description = "总人数")
    private Integer total;
}
