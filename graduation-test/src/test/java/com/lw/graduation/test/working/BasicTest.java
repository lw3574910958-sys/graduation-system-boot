package com.lw.graduation.test.working;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 基础测试类 - 验证测试环境
 */
class BasicTest {

    @Test
    void testBasicAssertions() {
        assertEquals(2, 1 + 1, "基本数学运算应该正确");
        assertTrue(true, "true应该为true");
        assertFalse(false, "false应该为false");
        assertNotNull("test", "字符串不应该为null");
    }

    @Test
    void testStringOperations() {
        String str = "hello";
        assertEquals("HELLO", str.toUpperCase(), "字符串转大写应该正确");
        assertEquals(5, str.length(), "字符串长度应该正确");
        assertTrue(str.contains("ell"), "字符串应该包含指定子串");
    }
}