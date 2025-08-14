package com.example.new_staff_field_be.controller;

import com.example.new_staff_field_be.entity.UserLogin;
import com.example.new_staff_field_be.repository.UserLoginRepository;
import com.example.new_staff_field_be.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserLoginControllerTest {

    @Mock
    private UserLoginRepository userLoginRepository;

    @InjectMocks
    private UserLoginController userLoginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkUserName_UserExists_ReturnsTrue() {
        when(userLoginRepository.findByUsername("testuser")).thenReturn(new UserLogin());

        ResponseEntity<Boolean> response = userLoginController.checkUserName("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    void checkUserName_UserNotExists_ReturnsFalse() {
        when(userLoginRepository.findByUsername("nonexistent")).thenReturn(null);

        ResponseEntity<Boolean> response = userLoginController.checkUserName("nonexistent");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    void registerUser_Success() {
        // 首先生成盐值并缓存
        ResponseEntity<String> saltResponse = userLoginController.generateSaltForUser("newuser");
        String salt = saltResponse.getBody();

        UserLogin userLogin = new UserLogin();
        userLogin.setUsername("newuser");
        userLogin.setPassword("frontendEncryptedPassword");

        when(userLoginRepository.save(any(UserLogin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = userLoginController.registerUser(userLogin);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("User registered successfully", responseBody.get("message"));
    }

    @Test
    void registerUser_MissingSaltInCache_ReturnsError() {
        UserLogin userLogin = new UserLogin();
        userLogin.setUsername("newuser");
        userLogin.setPassword("frontendEncryptedPassword");

        ResponseEntity<?> response = userLoginController.registerUser(userLogin);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertEquals("缓存错误", responseBody.get("message"));
    }

    @Test
    void loginUser_Success() {
        // 先注册一个用户
        ResponseEntity<String> saltResponse = userLoginController.generateSaltForUser("testuser");
        String salt = saltResponse.getBody();

        UserLogin newUser = new UserLogin();
        newUser.setUsername("testuser");
        newUser.setPassword("frontendEncryptedPassword");

        when(userLoginRepository.save(any(UserLogin.class))).thenAnswer(invocation -> {
            UserLogin savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // 设置ID
            return savedUser;
        });

        userLoginController.registerUser(newUser);

        // 然后测试登录
        UserLogin loginUser = new UserLogin();
        loginUser.setUsername("testuser");
        loginUser.setPassword("frontendEncryptedPassword");

        UserLogin existingUser = new UserLogin();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setSalt(salt);
        existingUser.setPassword(PasswordUtils.secondaryEncrypt("frontendEncryptedPassword", salt));

        when(userLoginRepository.findByUsername("testuser")).thenReturn(existingUser);

        ResponseEntity<?> response = userLoginController.loginUser(loginUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertNotNull(responseBody.get("user"));
    }

    @Test
    void loginUser_UserNotFound_ReturnsUnauthorized() {
        UserLogin userLogin = new UserLogin();
        userLogin.setUsername("nonexistent");
        userLogin.setPassword("password");

        when(userLoginRepository.findByUsername("nonexistent")).thenReturn(null);

        ResponseEntity<?> response = userLoginController.loginUser(userLogin);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, Boolean> responseBody = (Map<String, Boolean>) response.getBody();
        assertFalse(responseBody.get("success"));
    }

    @Test
    void loginUser_WrongPassword_ReturnsUnauthorized() {
        // 先注册一个用户
        ResponseEntity<String> saltResponse = userLoginController.generateSaltForUser("testuser");
        String salt = saltResponse.getBody();

        UserLogin newUser = new UserLogin();
        newUser.setUsername("testuser");
        newUser.setPassword("correctpassword");

        when(userLoginRepository.save(any(UserLogin.class))).thenReturn(newUser);
        userLoginController.registerUser(newUser);

        // 然后测试登录
        UserLogin loginUser = new UserLogin();
        loginUser.setUsername("testuser");
        loginUser.setPassword("wrongpassword");

        UserLogin existingUser = new UserLogin();
        existingUser.setUsername("testuser");
        existingUser.setSalt(salt);
        existingUser.setPassword(PasswordUtils.secondaryEncrypt("correctpassword", salt));

        when(userLoginRepository.findByUsername("testuser")).thenReturn(existingUser);

        ResponseEntity<?> response = userLoginController.loginUser(loginUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, Boolean> responseBody = (Map<String, Boolean>) response.getBody();
        assertFalse(responseBody.get("success"));
    }

    @Test
    void generateSaltForUser_ReturnsSalt() {
        ResponseEntity<String> response = userLoginController.generateSaltForUser("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(64, response.getBody().length()); // SHA-256 produces 64-character hex string
        assertTrue(response.getBody().matches("[a-f0-9]{64}"));
    }
}
