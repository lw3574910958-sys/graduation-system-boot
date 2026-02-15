package com.lw.graduation.api.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 院系更新参数
 *
 * @author lw
 */
@Data
@Schema(description = "院系更新参数")
public class DepartmentUpdateDTO {

    /**
     * 院系编码
     */
    @Schema(description = "院系编码", example = "CS001")
    private String code;

    /**
     * 院系名称
     */
    @Schema(description = "院系名称", example = "计算机科学与技术学院")
    private String name;
}