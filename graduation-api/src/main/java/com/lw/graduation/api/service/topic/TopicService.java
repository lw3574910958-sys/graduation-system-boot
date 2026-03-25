package com.lw.graduation.api.service.topic;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.topic.TopicPageQueryDTO;
import com.lw.graduation.api.dto.topic.TopicReviewDTO;
import com.lw.graduation.api.dto.topic.TopicUpdateDTO;
import com.lw.graduation.api.vo.topic.TopicVO;

import java.util.List;

/**
 * 课题服务接口
 * 定义课题管理模块的核心业务逻辑。
 *
 * @author lw
 */
public interface TopicService {

    /**
     * 分页查询课题列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<TopicVO> getTopicPage(TopicPageQueryDTO queryDTO);

    /**
     * 根据ID获取课题详情
     *
     * @param id 课题ID
     * @return 课题详情 VO
     */
    TopicVO getTopicById(Long id);

    /**
     * 创建新课题
     *
     * @param createDTO 创建课题 DTO
     */
    void createTopic(TopicCreateDTO createDTO);

    /**
     * 更新课题信息
     *
     * @param id        课题ID
     * @param updateDTO 更新课题 DTO
     */
    void updateTopic(Long id, TopicUpdateDTO updateDTO);

    /**
     * 撤销题目（仅草稿状态）
     *
     * @param id 课题 ID
     */
    void revokeTopic(Long id);

    /**
     * 删除课题
     *
     * @param id 课题 ID
     */
    void deleteTopic(Long id);

    /**
     * 教师提交题目审核
     *
     * @param topicId 题目 ID
     */
    void submitForReview(Long topicId);

    /**
     * 审核题目（院系管理员）
     *
     * @param reviewDTO 审核请求 DTO
     * @param reviewerId 审核人 ID
     */
    void reviewTopic(TopicReviewDTO reviewDTO, Long reviewerId);

    /**
     * 开放课题
     *
     * @param id 课题 ID
     */
    void openTopic(Long id);

    /**
     * 关闭课题
     *
     * @param id 课题 ID
     */
    void closeTopic(Long id);

    /**
     * 获取可选题目列表（开放状态且未满员的题目）
     * 学生选题功能的核心方法
     *
     * @param departmentId 院系 ID(null 表示所有院系)
     * @return 可选题目列表
     */
    List<TopicVO> getAvailableTopics(Long departmentId);

    /**
     * 教师获取自己发布的题目列表
     * 教师管理功能接口
     *
     * @param teacherId 教师 ID
     * @param status 题目状态 (null 表示所有状态)
     * @return 题目列表
     */
    List<TopicVO> getTopicsByTeacher(Long teacherId, Integer status);

    /**
     * 处理选题申请事件，更新题目状态
     *
     * @param topicId 题目 ID
     */
    void handleSelectionApplied(Long topicId);

    /**
     * 处理选题审核结果事件
     *
     * @param topicId 题目 ID
     * @param selectionApproved 审核是否通过
     */
    void handleSelectionReviewed(Long topicId, boolean selectionApproved);

    /**
     * 处理学生确认选题事件
     *
     * @param topicId 题目 ID
     */
    void handleSelectionConfirmed(Long topicId);
}