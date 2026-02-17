package com.lw.graduation.api.service.selection;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.selection.SelectionApplyDTO;
import com.lw.graduation.api.dto.selection.SelectionPageQueryDTO;
import com.lw.graduation.api.dto.selection.SelectionReviewDTO;
import com.lw.graduation.api.vo.selection.SelectionVO;

import java.util.List;

/**
 * 选题服务接口
 * 定义选题管理模块的核心业务逻辑，包括申请、审核、确认等完整流程。
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
     * 学生申请选题
     *
     * @param applyDTO 申请DTO
     * @param studentId 学生ID
     * @return 申请结果
     */
    SelectionVO applySelection(SelectionApplyDTO applyDTO, Long studentId);

    /**
     * 教师审核选题申请
     *
     * @param reviewDTO 审核DTO
     * @param teacherId 教师ID
     * @return 审核结果
     */
    SelectionVO reviewSelection(SelectionReviewDTO reviewDTO, Long teacherId);

    /**
     * 学生确认选题
     *
     * @param selectionId 选题ID
     * @param studentId 学生ID
     * @return 确认结果
     */
    SelectionVO confirmSelection(Long selectionId, Long studentId);

    /**
     * 获取学生的所有选题申请
     *
     * @param studentId 学生ID
     * @return 选题列表
     */
    List<SelectionVO> getSelectionsByStudent(Long studentId);

    /**
     * 获取教师需要审核的选题申请
     *
     * @param teacherId 教师ID
     * @return 选题列表
     */
    List<SelectionVO> getSelectionsForReview(Long teacherId);

    /**
     * 撤销选题申请
     *
     * @param selectionId 选题ID
     * @param studentId 学生ID
     * @return 撤销结果
     */
    boolean cancelSelection(Long selectionId, Long studentId);

    /**
     * 删除选题记录
     *
     * @param id 选题ID
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteSelection(Long id, Long userId);
}