package com.lw.graduation.infrastructure.mapper.grade;

import com.lw.graduation.domain.entity.grade.BizGrade;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 成绩表 Mapper 接口
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
public interface BizGradeMapper extends MyBaseMapper<BizGrade> {

    /**
     * 批量查询成绩详情（包含关联信息）
     * 通过JOIN一次性获取成绩、学生、用户、题目、评分教师等关联信息
     * 
     * @param ids 成绩ID列表
     * @return 成绩详情Map列表
     */
    @Select({
        "<script>",
        "SELECT g.id, g.student_id, g.topic_id, g.score, g.grader_id, g.comment, g.graded_at, g.created_at, g.updated_at,",
        "       s.student_id as student_number, u.real_name as student_name, t.title as topic_title, gr.real_name as grader_name",
        "FROM biz_grade g",
        "LEFT JOIN biz_student s ON g.student_id = s.id",
        "LEFT JOIN sys_user u ON s.user_id = u.id",
        "LEFT JOIN biz_topic t ON g.topic_id = t.id",
        "LEFT JOIN sys_user gr ON g.grader_id = gr.id",
        "WHERE g.id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "AND g.is_deleted = 0",
        "</script>"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "topicId", column = "topic_id"),
        @Result(property = "score", column = "score"),
        @Result(property = "graderId", column = "grader_id"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "gradedAt", column = "graded_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "studentNumber", column = "student_number"),
        @Result(property = "studentName", column = "student_name"),
        @Result(property = "topicTitle", column = "topic_title"),
        @Result(property = "graderName", column = "grader_name")
    })
    List<Map<String, Object>> selectGradeDetailsWithRelations(@Param("ids") List<Long> ids);

    /**
     * 根据学生ID查询成绩详情
     * 
     * @param studentId 学生ID
     * @return 成绩详情Map列表
     */
    @Select({
        "SELECT g.id, g.student_id, g.topic_id, g.score, g.grader_id, g.comment, g.graded_at, g.created_at, g.updated_at,",
        "       s.student_id as student_number, u.real_name as student_name, t.title as topic_title, gr.real_name as grader_name",
        "FROM biz_grade g",
        "LEFT JOIN biz_student s ON g.student_id = s.id",
        "LEFT JOIN sys_user u ON s.user_id = u.id",
        "LEFT JOIN biz_topic t ON g.topic_id = t.id",
        "LEFT JOIN sys_user gr ON g.grader_id = gr.id",
        "WHERE g.student_id = #{studentId} AND g.is_deleted = 0",
        "ORDER BY g.created_at DESC"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "topicId", column = "topic_id"),
        @Result(property = "score", column = "score"),
        @Result(property = "graderId", column = "grader_id"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "gradedAt", column = "graded_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "studentNumber", column = "student_number"),
        @Result(property = "studentName", column = "student_name"),
        @Result(property = "topicTitle", column = "topic_title"),
        @Result(property = "graderName", column = "grader_name")
    })
    List<Map<String, Object>> selectByStudentIdWithDetails(@Param("studentId") Long studentId);

    /**
     * 根据教师ID查询成绩详情（该教师评分的成绩）
     * 
     * @param graderId 评分教师ID
     * @return 成绩详情Map列表
     */
    @Select({
        "SELECT g.id, g.student_id, g.topic_id, g.score, g.grader_id, g.comment, g.graded_at, g.created_at, g.updated_at,",
        "       s.student_id as student_number, u.real_name as student_name, t.title as topic_title, gr.real_name as grader_name",
        "FROM biz_grade g",
        "LEFT JOIN biz_student s ON g.student_id = s.id",
        "LEFT JOIN sys_user u ON s.user_id = u.id",
        "LEFT JOIN biz_topic t ON g.topic_id = t.id",
        "LEFT JOIN sys_user gr ON g.grader_id = gr.id",
        "WHERE g.grader_id = #{graderId} AND g.is_deleted = 0",
        "ORDER BY g.created_at DESC"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "topicId", column = "topic_id"),
        @Result(property = "score", column = "score"),
        @Result(property = "graderId", column = "grader_id"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "gradedAt", column = "graded_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "studentNumber", column = "student_number"),
        @Result(property = "studentName", column = "student_name"),
        @Result(property = "topicTitle", column = "topic_title"),
        @Result(property = "graderName", column = "grader_name")
    })
    List<Map<String, Object>> selectByGraderIdWithDetails(@Param("graderId") Long graderId);
}
