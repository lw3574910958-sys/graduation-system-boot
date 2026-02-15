package com.lw.graduation.api.dto.document;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档分页查询参数")
public class DocumentPageQueryDTO extends BasePageQueryDTO {

    /**
     * 上传人ID
     */
    @Schema(description = "上传人ID")
    private Long userId;

    /**
     * 关联题目ID
     */
    @Schema(description = "关联题目ID")
    private Long topicId;

    /**
     * 文件类型: 0-开题报告, 1-中期报告, 2-毕业论文
     */
    @Schema(description = "文件类型")
    private Integer fileType;

    /**
     * 审核状态: 0-待审, 1-通过, 2-驳回
     */
    @Schema(description = "审核状态")
    private Integer reviewStatus;
}