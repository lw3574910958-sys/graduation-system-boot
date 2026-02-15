package com.lw.graduation.api.dto.department;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 院系创建参数
 *
 * @author lw
 */
@Data
@Schema(description = "院系创建参数")
public class DepartmentCreateDTO {

    /**
     * 院系编码
     */
    @NotBlank(message = "院系编码不能为空")
    @Schema(description = "院系编码", example = "CS001")
    private String code;

    /**
     * 院系名称
     */
    @NotBlank(message = "院系名称不能为空")
    @Schema(description = "院系名称", example = "计算机科学与技术学院")
    private String name;
}