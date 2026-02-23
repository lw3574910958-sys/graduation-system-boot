package com.lw.graduation.api.service.topic;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.topic.TopicPageQueryDTO;
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
     * 删除课题
     *
     * @param id 课题ID
     */
    void deleteTopic(Long id);
    
    /**
     * 获取可选的课题列表
     * 只返回开放和审核中状态的题目
     *
     * @param departmentId 院系ID（可选）
     * @return 可选课题列表
     */
    List<TopicVO> getSelectableTopics(Long departmentId);
}