package com.lw.graduation.common.base;

// import io.swagger.v3.oas.annotations.media.Schema;
import com.lw.graduation.common.constant.CommonConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基础DTO
 * 所有分页查询DTO都应该继承此类
 *
 * @author lw
 */
@Data
//@Schema(description = "分页查询基础参数")
public class BasePageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Min(value = CommonConstants.Numbers.MIN_SIZE, message = "页码必须大于0")
    //@Schema(description = "当前页码", defaultValue = "1")
    private Integer current = CommonConstants.Numbers.DEFAULT_PAGE;

    /**
     * 每页大小
     */
    @Min(value = CommonConstants.Numbers.MIN_SIZE, message = "每页数量必须大于0")
    @Max(value = CommonConstants.Numbers.MAX_SIZE, message = "每页数量不能超过100")
    //@Schema(description = "每页大小", defaultValue = "10")
    private Integer size = CommonConstants.Numbers.DEFAULT_SIZE;
}