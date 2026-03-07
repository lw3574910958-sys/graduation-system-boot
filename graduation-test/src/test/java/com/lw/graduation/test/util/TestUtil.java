package com.lw.graduation.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试工具类
 * 提供常用的测试辅助方法
 *
 * @author lw
 */
public class TestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行GET请求并验证成功响应
     */
    public static ResultActions performGet(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行POST请求并验证成功响应
     */
    public static ResultActions performPost(MockMvc mockMvc, String url, Object requestBody) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行PUT请求并验证成功响应
     */
    public static ResultActions performPut(MockMvc mockMvc, String url, Object requestBody) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行DELETE请求并验证成功响应
     */
    public static ResultActions performDelete(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(delete(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * 验证JSON响应中的字段值
     */
    public static ResultActions expectJsonField(ResultActions resultActions, String jsonPath, Object expectedValue) throws Exception {
        return resultActions.andExpect(jsonPath(jsonPath).value(expectedValue));
    }

    /**
     * 验证JSON响应中存在某个字段
     */
    public static ResultActions expectJsonFieldExists(ResultActions resultActions, String jsonPath) throws Exception {
        return resultActions.andExpect(jsonPath(jsonPath).exists());
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 从JSON字符串创建对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}