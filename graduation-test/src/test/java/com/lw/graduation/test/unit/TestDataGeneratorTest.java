package com.lw.graduation.test.unit;

import com.lw.graduation.test.util.TestDataGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具类单元测试示例
 */
class TestDataGeneratorTest {

    @Test
    void testCreateRandomEmail() {
        String email1 = TestDataGenerator.createRandomEmail();
        // 添加小延迟确保不同
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        String email2 = TestDataGenerator.createRandomEmail();
        
        assertNotNull(email1);
        assertNotNull(email2);
        assertTrue(email1.contains("@"));
        assertTrue(email2.contains("@"));
        assertNotEquals(email1, email2, "应该生成不同的邮箱");
    }

    @Test
    void testCreateRandomUsername() {
        String username1 = TestDataGenerator.createRandomUsername();
        // 添加小延迟确保不同
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        String username2 = TestDataGenerator.createRandomUsername();
        
        assertNotNull(username1);
        assertNotNull(username2);
        assertTrue(username1.startsWith("testuser"));
        assertTrue(username2.startsWith("testuser"));
        assertNotEquals(username1, username2, "应该生成不同的用户名");
    }

    @Test
    void testCreateTestDateTime() {
        var dateTime = TestDataGenerator.createTestDateTime();
        
        assertNotNull(dateTime);
        assertEquals(2024, dateTime.getYear());
        assertEquals(1, dateTime.getMonthValue());
        assertEquals(1, dateTime.getDayOfMonth());
    }
}