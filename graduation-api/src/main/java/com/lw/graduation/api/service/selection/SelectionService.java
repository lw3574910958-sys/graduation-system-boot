package com.lw.graduation.api.service.selection;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.selection.SelectionCreateDTO;
import com.lw.graduation.api.dto.selection.SelectionPageQueryDTO;
import com.lw.graduation.api.dto.selection.SelectionUpdateDTO;
import com.lw.graduation.api.vo.selection.SelectionVO;

/**
 * 选题服务接口
 * 定义选题管理模块的核心业务逻辑。
 *
 * @author lw
 */
public interface SelectionService {

    /**
     * 分页查询选题列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<SelectionVO> getSelectionPage(SelectionPageQueryDTO queryDTO);

    /**
     * 根据ID获取选题详情
     *
     * @param id 选题ID
     * @return 选题详情 VO
     */
    SelectionVO getSelectionById(Long id);

    /**
     * 创建新选题
     *
     * @param createDTO 创建选题 DTO
     */
    void createSelection(SelectionCreateDTO createDTO);

    /**
     * 更新选题信息
     *
     * @param id        选题ID
     * @param updateDTO 更新选题 DTO
     */
    void updateSelection(Long id, SelectionUpdateDTO updateDTO);

    /**
     * 删除选题
     *
     * @param id 选题ID
     */
    void deleteSelection(Long id);
}