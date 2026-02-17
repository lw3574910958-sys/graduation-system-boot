package com.lw.graduation.api.dto.document;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 文档分页查询DTO
 * 用于文档分页查询的数据传输对象
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档分页查询DTO")
public class DocumentPageQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（查询某个用户的文档）
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 题目ID（查询某个题目的文档）
     */
    @Schema(description = "题目ID")
    private Long topicId;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型: 0-开题报告, 1-中期报告, 2-毕业论文, 3-外文翻译, 4-其他文档")
    private Integer fileType;

    /**
     * 审核状态
     */
    @Schema(description = "审核状态: 0-待审, 1-通过, 2-驳回")
    private Integer reviewStatus;

    /**
     * 关键词搜索（文档名称）
     */
    @Schema(description = "关键词搜索")
    private String keyword;

    /**
     * 院系ID（按院系查询）
     */
    @Schema(description = "院系ID")
    private Long departmentId;
}