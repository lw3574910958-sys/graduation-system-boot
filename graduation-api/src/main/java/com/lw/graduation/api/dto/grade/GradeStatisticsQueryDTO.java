package com.lw.graduation.api.dto.grade;

import com.lw.graduation.common.base.BasePageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 成绩统计查询DTO
 * 用于查询成绩统计信息的数据传输对象
 *
 * @author lw
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "成绩统计查询DTO")
public class GradeStatisticsQueryDTO extends BasePageQueryDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID（按题目统计）
     */
    @Schema(description = "题目ID")
    private Long topicId;

    /**
     * 教师ID（查询某个教师指导学生的成绩）
     */
    @Schema(description = "教师ID")
    private Long teacherId;

    /**
     * 院系ID（按院系统计）
     */
    @Schema(description = "院系ID")
    private Long departmentId;

    /**
     * 年份（按年份统计）
     */
    @Schema(description = "年份")
    private Integer year;

    /**
     * 成绩等级筛选
     */
    @Schema(description = "成绩等级: excellent,good,fair,pass,fail")
    private String gradeLevel;

    /**
     * 是否只统计及格成绩
     */
    @Schema(description = "是否只统计及格成绩")
    private Boolean passingOnly = false;

    /**
     * 统计类型 (1-个人统计, 2-班级统计, 3-院系统计, 4-全校统计)
     */
    @Schema(description = "统计类型: 1-个人统计, 2-班级统计, 3-院系统计, 4-全校统计")
    private Integer statisticsType = 2;
}