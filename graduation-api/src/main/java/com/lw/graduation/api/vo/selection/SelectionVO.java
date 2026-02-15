package com.lw.graduation.api.vo.selection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lw.graduation.common.constant.CommonConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选题信息视图对象 (View Object)
 * 用于向外部（如前端）展示选题的详细信息。
 * 对应领域层的 BizSelection 实体。
 *
 * @author lw
 */
@Data
@Schema(description = "选题信息视图对象")
public class SelectionVO {

    /**
     * 选题ID
     */
    private Long id;

    /**
     * 学生ID
     */
    @Schema(description = "学生ID")
    private Long studentId;

    /**
     * 学生姓名
     */
    @Schema(description = "学生姓名")
    private String studentName;

    /**
     * 课题ID
     */
    @Schema(description = "课题ID")
    private Long topicId;

    /**
     * 课题标题
     */
    @Schema(description = "课题标题")
    private String topicTitle;

    /**
     * 状态 (0-待确认, 1-已确认)
     */
    @Schema(description = "状态 (0-待确认, 1-已确认)")
    private Integer status;

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