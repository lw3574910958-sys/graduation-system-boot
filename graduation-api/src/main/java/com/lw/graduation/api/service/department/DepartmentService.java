package com.lw.graduation.api.service.department;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.department.DepartmentCreateDTO;
import com.lw.graduation.api.dto.department.DepartmentPageQueryDTO;
import com.lw.graduation.api.dto.department.DepartmentUpdateDTO;
import com.lw.graduation.api.vo.department.DepartmentVO;

/**
 * 院系服务接口
 *
 * @author lw
 */
public interface DepartmentService {

    /**
     * 分页查询院系列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<DepartmentVO> getDepartmentPage(DepartmentPageQueryDTO queryDTO);

    /**
     * 根据ID获取院系详情
     *
     * @param id 院系ID
     * @return 院系详情
     */
    DepartmentVO getDepartmentById(Long id);

    /**
     * 创建院系
     *
     * @param createDTO 创建参数
     */
    void createDepartment(DepartmentCreateDTO createDTO);

    /**
     * 更新院系
     *
     * @param id 院系ID
     * @param updateDTO 更新参数
     */
    void updateDepartment(Long id, DepartmentUpdateDTO updateDTO);

    /**
     * 删除院系
     *
     * @param id 院系ID
     */
    void deleteDepartment(Long id);

    /**
     * 获取所有院系列表（用于下拉框等场景）
     *
     * @return 院系列表
     */
    java.util.List<DepartmentVO> getAllDepartments();
}