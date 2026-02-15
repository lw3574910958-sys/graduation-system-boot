package com.lw.graduation.api.dto.department;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 院系分页查询参数
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "院系分页查询参数")
public class DepartmentPageQueryDTO extends BasePageQueryDTO {

    /**
     * 院系编码
     */
    @Schema(description = "院系编码")
    private String code;

    /**
     * 院系名称
     */
    @Schema(description = "院系名称")
    private String name;
}