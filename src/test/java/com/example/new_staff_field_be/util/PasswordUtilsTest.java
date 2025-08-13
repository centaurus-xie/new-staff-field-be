package com.example.new_staff_field_be.util;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {

    @Test
    public void testGenerateSalt_ShouldReturnConsistentHashForSameUsername() {
        // 给定相同的用户名，盐值应该一致
        String username = "alice123";
        String salt1 = PasswordUtils.generateSalt(username);
        String salt2 = PasswordUtils.generateSalt(username);

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(salt1, salt2); // 同样的输入应产生同样的输出
        assertEquals(64, salt1.length()); // SHA-256 输出是 64 位十六进制字符串
    }

    @Test
    public void testGenerateSalt_DifferentUsernames_ShouldProduceDifferentSalts() {
        String salt1 = PasswordUtils.generateSalt("alice");
        String salt2 = PasswordUtils.generateSalt("bob");

        assertNotEquals(salt1, salt2);
    }

    @Test
    public void testSecondaryEncrypt_ShouldBeDeterministic() {
        String frontendEncryptedPassword = "abc123def456"; // 模拟前端加密后的密码
        String salt = "salt123";

        String encrypted1 = PasswordUtils.secondaryEncrypt(frontendEncryptedPassword, salt);
        String encrypted2 = PasswordUtils.secondaryEncrypt(frontendEncryptedPassword, salt);

        assertNotNull(encrypted1);
        assertEquals(encrypted1, encrypted2); // 同样的输入应得到相同结果
        assertEquals(64, encrypted1.length());
    }

    @Test
    public void testSecondaryEncrypt_WithRealSaltFromUsername() {
        String frontendEncryptedPassword = "frontend_hash_123";
        String username = "charlie";
        String salt = PasswordUtils.generateSalt(username);

        String result = PasswordUtils.secondaryEncrypt(frontendEncryptedPassword, salt);

        assertNotNull(result);
        assertEquals(64, result.length());
        assertTrue(result.matches("[a-f0-9]{64}")); // 确保是合法的十六进制字符串
    }

    @Test
    public void testSecondaryEncrypt_NullInput_ShouldThrowException() {
        String salt = PasswordUtils.generateSalt("user");

        // 测试前端密码为 null
        Exception exception1 = assertThrows(NullPointerException.class, () -> {
            PasswordUtils.secondaryEncrypt(null, salt);
        });
        // 注意：实际会抛 NPE，因为 null.getBytes() 不合法

        // 测试 salt 为 null
        Exception exception2 = assertThrows(NullPointerException.class, () -> {
            PasswordUtils.secondaryEncrypt("password", null);
        });
    }
}
