package com.lw.graduation.api.service.grade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.grade.GradeInputDTO;
import com.lw.graduation.api.dto.grade.GradePageQueryDTO;
import com.lw.graduation.api.dto.grade.GradeStatisticsQueryDTO;
import com.lw.graduation.api.vo.grade.GradeExportVO;
import com.lw.graduation.api.vo.grade.GradeVO;
import org.springframework.http.ResponseEntity;

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
     * 录入成绩（首次录入）
     * 教师对已创建的成绩记录进行首次评分，仅允许填写分数和评语
     * 每个学生的每个课题每种成绩类型只能录入一次，录入后不允许修改
     *
     * @param inputDTO 成绩录入 DTO
     * @param graderId 评分教师 ID
     * @return 录入的成绩 VO
     */
    GradeVO inputGrade(GradeInputDTO inputDTO, Long graderId);
    
    /**
     * 自动创建成绩记录（不设置评分时间）
     * 用于文档审核通过后自动创建评分记录，由教师在正式评分时设置评分时间
     *
     * @param inputDTO 成绩录入 DTO
     * @param graderId 评分教师 ID
     */
    void inputGradeForAutoCreate(GradeInputDTO inputDTO, Long graderId);

    /**
     * 自动计算综合成绩
     *
     * @param studentId 学生 ID
     * @param topicId 题目 ID
     * @return 计算后的综合成绩
     */
    BigDecimal calculateCompositeGrade(Long studentId, Long topicId);
    
    /**
     * 尝试自动保存综合成绩（独立方法）
     * 当开题、中期、毕业论文都已完成评分时，自动计算并保存综合成绩
     * 该方法可独立调用，不依赖于成绩录入流程
     *
     * @param studentId 学生 ID
     * @param topicId 题目 ID
     * @param graderId 评分教师 ID（用于记录综合成绩的评分人）
     */
    void tryAutoSaveCompositeGrade(Long studentId, Long topicId, Long graderId);

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
     * @return 成绩分布统计 JSON 字符串
     */
    String getGradeStatistics(GradeStatisticsQueryDTO queryDTO);

    /**
     * 导出成绩数据为 Excel
     *
     * @param queryDTO 查询条件
     * @return 导出数据列表
     */
    List<GradeExportVO> exportGrades(GradePageQueryDTO queryDTO);

    /**
     * 导出成绩报表响应（包含完整的HTTP响应头设置）
     *
     * @param queryDTO 查询条件
     * @return ResponseEntity<byte[]>
     */
    ResponseEntity<byte[]> exportGradesResponse(GradePageQueryDTO queryDTO);
}