package com.lw.graduation.test.util;

import com.lw.graduation.api.dto.auth.LoginDTO;
import com.lw.graduation.api.dto.department.DepartmentCreateDTO;
import com.lw.graduation.api.dto.topic.TopicCreateDTO;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.domain.enums.user.AccountStatus;
import com.lw.graduation.domain.enums.permission.SystemRole;
import com.lw.graduation.domain.enums.status.TopicStatus;

import java.time.LocalDateTime;

/**
 * 测试数据生成工具类
 * 提供常用的测试数据构建方法
 *
 * @author lw
 */
public class TestDataGenerator {

    /**
     * 创建登录DTO
     */
    public static LoginDTO createLoginDTO(String username, String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        loginDTO.setCaptchaCode("123456");
        loginDTO.setCaptchaKey("test-key");
        return loginDTO;
    }

    /**
     * 创建用户创建DTO
     */
    public static UserCreateDTO createUserCreateDTO(String username, String email) {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setUsername(username);
        userCreateDTO.setPassword("123456");
        userCreateDTO.setRealName("测试用户");
        userCreateDTO.setUserType("student");
        userCreateDTO.setStatus(1);
        userCreateDTO.setAvatar("test-avatar.jpg");
        return userCreateDTO;
    }

    /**
     * 创建院系创建DTO
     */
    public static DepartmentCreateDTO createDepartmentCreateDTO(String name, String code) {
        DepartmentCreateDTO departmentCreateDTO = new DepartmentCreateDTO();
        departmentCreateDTO.setName(name);
        departmentCreateDTO.setCode(code);

        return departmentCreateDTO;
    }

    /**
     * 创建课题创建DTO
     */
    public static TopicCreateDTO createTopicCreateDTO(String title, Long teacherId) {
        TopicCreateDTO topicCreateDTO = new TopicCreateDTO();
        topicCreateDTO.setTitle(title);
        topicCreateDTO.setDescription("测试课题描述");
        topicCreateDTO.setSource("测试来源");
        topicCreateDTO.setType("应用研究");
        topicCreateDTO.setNature("理论研究");
        topicCreateDTO.setDifficulty(3);
        topicCreateDTO.setWorkload(3);
        topicCreateDTO.setMaxSelections(3);

        return topicCreateDTO;
    }

    /**
     * 创建测试时间
     */
    public static LocalDateTime createTestDateTime() {
        return LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    /**
     * 创建随机邮箱
     */
    public static String createRandomEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * 创建随机用户名
     */
    public static String createRandomUsername() {
        return "testuser" + System.currentTimeMillis();
    }
}