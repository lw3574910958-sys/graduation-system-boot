package com.lw.graduation.api.service.grade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.grade.GradeCreateDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeUpdateDTO;
import com.lw.graduation.api.vo.grade.GradeVO;

/**
 * 成绩服务接口
 * 定义成绩管理模块的核心业务逻辑。
 *
 * @author lw
 */
public interface GradeService {

    /**
     * 分页查询成绩列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<GradeVO> getGradePage(GradePageQueryDTO queryDTO);

    /**
     * 根据ID获取成绩详情
     *
     * @param id 成绩ID
     * @return 成绩详情 VO
     */
    GradeVO getGradeById(Long id);

    /**
     * 创建新成绩
     *
     * @param createDTO 创建成绩 DTO
     */
    void createGrade(GradeCreateDTO createDTO);

    /**
     * 更新成绩信息
     *
     * @param id        成绩ID
     * @param updateDTO 更新成绩 DTO
     */
    void updateGrade(Long id, GradeUpdateDTO updateDTO);

    /**
     * 删除成绩
     *
     * @param id 成绩ID
     */
    void deleteGrade(Long id);
}