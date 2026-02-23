package com.lw.graduation.api.service.grade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeStatisticsQueryDTO;
import com.lw.graduation.api.vo.grade.GradeVO;

import java.math.BigDecimal;
import java.util.List;

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
     * 录入成绩
     *
     * @param inputDTO 成绩录入DTO
     * @param graderId 评分教师ID
     * @return 录入的成绩VO
     */
    GradeVO inputGrade(GradeInputDTO inputDTO, Long graderId);

    /**
     * 自动计算综合成绩
     *
     * @param studentId 学生ID
     * @param topicId 题目ID
     * @return 计算后的综合成绩
     */
    BigDecimal calculateCompositeGrade(Long studentId, Long topicId);

    /**
     * 获取学生的所有成绩
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<GradeVO> getGradesByStudent(Long studentId);

    /**
     * 获取教师指导学生的成绩
     *
     * @param teacherId 教师ID
     * @return 成绩列表
     */
    List<GradeVO> getGradesByTeacher(Long teacherId);

    /**
     * 获取成绩统计信息
     *
     * @param queryDTO 统计查询条件
     * @return 成绩分布统计JSON字符串
     */
    String getGradeStatistics(GradeStatisticsQueryDTO queryDTO);

    /**
     * 删除成绩
     *
     * @param id 成绩ID
     * @param graderId 教师ID
     */
    void deleteGrade(Long id, Long graderId);
}