package com.lw.graduation.test.simple;

import com.lw.graduation.test.config.TestConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单测试验证测试环境是否正常工作
 */
@TestConfig
class SimpleTest {

    @Test
    void testBasicFunctionality() {
        // 基本的功能测试
        String testString = "hello";
        assertEquals("hello", testString);
        assertTrue(testString.length() > 0);
    }

    @Test
    void testMathOperations() {
        int result = 2 + 3;
        assertEquals(5, result);
    }
}