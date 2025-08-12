package com.example.new_staff_field_be.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * 根据用户名生成盐值（使用SHA-256哈希确保唯一性）
     */
    public static String generateSalt(String username) {
        return sha256(username);
    }

    /**
     * 二次加密：使用盐值对前端传来的加密密码进行SHA-256加密
     * @param frontendEncryptedPassword 前端已加密的密码
     * @param salt 后端生成的盐值
     * @return 二次加密后的密码（存入数据库）
     */
    public static String secondaryEncrypt(String frontendEncryptedPassword, String salt) {
        return sha256(frontendEncryptedPassword + salt); // 密码+盐值拼接后加密
    }

    /**
     * SHA-256加密核心方法
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // 转16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256加密失败", e);
        }
    }
}
