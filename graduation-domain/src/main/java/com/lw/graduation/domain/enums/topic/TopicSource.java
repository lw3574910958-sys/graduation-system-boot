package com.lw.graduation.domain.enums.topic;

import com.lw.graduation.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目来源枚举
 * 定义毕业设计的题目来源渠道
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum TopicSource implements IEnum<String> {

    /**
     * 教学实践 - 来源于日常教学过程中的实践需求
     */
    TEACHING_PRACTICE("教学实践", "教学实践"),

    /**
     * 科研项目 - 来源于教师或实验室的科研项目
     */
    SCIENTIFIC_RESEARCH("科研项目", "科研项目"),

    /**
     * 企业合作 - 来源于与企业的合作项目
     */
    ENTERPRISE_COOPERATION("企业合作", "企业合作"),

    /**
     * 前沿技术 - 来源于行业前沿技术研究
     */
    FRONTIER_TECHNOLOGY("前沿技术", "前沿技术"),

    /**
     * 社会实践 - 来源于社会实践活动
     */
    SOCIAL_PRACTICE("社会实践", "社会实践"),

    /**
     * 其他 - 其他来源
     */
    OTHER("其他", "其他");

    /**
     * Code(数据库存储值)
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

}
