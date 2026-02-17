package com.lw.graduation.infrastructure.mapper.document;

import com.lw.graduation.domain.entity.document.BizDocument;
import com.lw.graduation.infrastructure.mapper.MyBaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文档表 Mapper 接口
 * </p>
 *
 * @author lw
 * @since 2025-12-30
 */
public interface BizDocumentMapper extends MyBaseMapper<BizDocument> {

    /**
     * 批量查询文档详情（包含关联信息）
     * 通过JOIN一次性获取文档、用户、题目、审核人等关联信息
     * 
     * @param ids 文档ID列表
     * @return 文档详情Map列表
     */
    @Select({
        "<script>",
        "SELECT d.id, d.user_id, d.topic_id, d.file_type, d.original_filename, d.stored_path, d.file_size,",
        "       d.review_status, d.reviewed_at, d.reviewer_id, d.feedback, d.uploaded_at, d.created_at, d.updated_at,",
        "       u.real_name as user_name, t.title as topic_title, r.real_name as reviewer_name",
        "FROM biz_document d",
        "LEFT JOIN sys_user u ON d.user_id = u.id",
        "LEFT JOIN biz_topic t ON d.topic_id = t.id",
        "LEFT JOIN sys_user r ON d.reviewer_id = r.id",
        "WHERE d.id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "AND d.is_deleted = 0",
        "</script>"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "topicId", column = "topic_id"),
        @Result(property = "fileType", column = "file_type"),
        @Result(property = "originalFilename", column = "original_filename"),
        @Result(property = "storedPath", column = "stored_path"),
        @Result(property = "fileSize", column = "file_size"),
        @Result(property = "reviewStatus", column = "review_status"),
        @Result(property = "reviewedAt", column = "reviewed_at"),
        @Result(property = "reviewerId", column = "reviewer_id"),
        @Result(property = "feedback", column = "feedback"),
        @Result(property = "uploadedAt", column = "uploaded_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "userName", column = "user_name"),
        @Result(property = "topicTitle", column = "topic_title"),
        @Result(property = "reviewerName", column = "reviewer_name")
    })
    List<Map<String, Object>> selectDocumentDetailsWithRelations(@Param("ids") List<Long> ids);

    /**
     * 根据用户ID查询文档详情
     * 
     * @param userId 用户ID
     * @return 文档详情Map列表
     */
    @Select({
        "SELECT d.id, d.user_id, d.topic_id, d.file_type, d.original_filename, d.stored_path, d.file_size,",
        "       d.review_status, d.reviewed_at, d.reviewer_id, d.feedback, d.uploaded_at, d.created_at, d.updated_at,",
        "       u.real_name as user_name, t.title as topic_title, r.real_name as reviewer_name",
        "FROM biz_document d",
        "LEFT JOIN sys_user u ON d.user_id = u.id",
        "LEFT JOIN biz_topic t ON d.topic_id = t.id",
        "LEFT JOIN sys_user r ON d.reviewer_id = r.id",
        "WHERE d.user_id = #{userId} AND d.is_deleted = 0",
        "ORDER BY d.uploaded_at DESC"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "topicId", column = "topic_id"),
        @Result(property = "fileType", column = "file_type"),
        @Result(property = "originalFilename", column = "original_filename"),
        @Result(property = "storedPath", column = "stored_path"),
        @Result(property = "fileSize", column = "file_size"),
        @Result(property = "reviewStatus", column = "review_status"),
        @Result(property = "reviewedAt", column = "reviewed_at"),
        @Result(property = "reviewerId", column = "reviewer_id"),
        @Result(property = "feedback", column = "feedback"),
        @Result(property = "uploadedAt", column = "uploaded_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "userName", column = "user_name"),
        @Result(property = "topicTitle", column = "topic_title"),
        @Result(property = "reviewerName", column = "reviewer_name")
    })
    List<Map<String, Object>> selectByUserIdWithDetails(@Param("userId") Long userId);
}
