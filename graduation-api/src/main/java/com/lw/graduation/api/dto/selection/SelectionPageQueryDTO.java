package com.lw.graduation.api.dto.selection;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 选题分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "选题分页查询参数")
public class SelectionPageQueryDTO extends BasePageQueryDTO {

    /**
     * 学生 ID
     */
    @Schema(description = "学生 ID")
    private Long studentId;
    
    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;
    
    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNumber;
    
    /**
     * 课题 ID
     */
    @Schema(description = "课题 ID")
    private Long topicId;

    /**
     * 课题标题
     */
    @Schema(description = "课题标题")
    private String topicTitle;

    /**
     * 状态 (0-待审核, 1-审核通过, 2-审核驳回, 3-已确认)
     */
    @Schema(description = "状态")
    private Integer status;
}