package com.lw.graduation.api.vo.grade;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 成绩导出 Excel VO
 * 用于将成绩数据导出为 Excel 文件
 *
 * @author lw
 */
@Data
public class GradeExportVO {

    @ExcelProperty(value = "学生姓名", index = 0)
    private String studentName;

    @ExcelProperty(value = "学号", index = 1)
    private String studentNumber;

    @ExcelProperty(value = "课题标题", index = 2)
    private String topicTitle;

    @ExcelProperty(value = "成绩类型", index = 3)
    private String gradeTypeDesc;

    @ExcelProperty(value = "成绩分数", index = 4)
    private Double score;

    @ExcelProperty(value = "成绩等级", index = 5)
    private String gradeLevel;

    @ExcelProperty(value = "绩点", index = 6)
    private Double gpa;

    @ExcelProperty(value = "评分教师", index = 7)
    private String graderDisplay;

    @ExcelProperty(value = "评分时间", index = 8)
    private String gradedAt;

    @ExcelProperty(value = "评语", index = 9)
    private String comment;
}
