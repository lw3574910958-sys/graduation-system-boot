package com.lw.graduation.department.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.department.DepartmentCreateDTO;
import com.lw.graduation.api.dto.department.DepartmentPageQueryDTO;
import com.lw.graduation.api.dto.department.DepartmentUpdateDTO;
import com.lw.graduation.api.vo.department.DepartmentVO;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 院系服务接口
 * 定义院系管理模块的核心业务逻辑接口。
 *
 * @author lw
 */
public interface DepartmentService extends IService<SysDepartment> {
    
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
     * 获取所有院系列表
     *
     * @return 院系列表
     */
    List<DepartmentVO> getAllDepartments();
}