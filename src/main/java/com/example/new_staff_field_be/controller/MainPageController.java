package com.example.new_staff_field_be.controller;

import com.example.new_staff_field_be.entity.UserLogin;
import com.example.new_staff_field_be.repository.UserLoginRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // 允许前端地址跨域
@RequestMapping("/api/user")
public class MainPageController {
    @Autowired
    private UserLoginRepository userLoginRepository;

    @GetMapping("/getProfile")
    public ResponseEntity<?> getProfile(HttpSession session) {

        String username = (String) session.getAttribute("loggedInUser");
        System.out.println(username);
        if (username == null) {
            return ResponseEntity.status(401).body("用户未登录");
        }

        // 根据用户名查找用户信息
        UserLogin user = userLoginRepository.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        // 清除敏感字段
        user.setPassword(null);
        user.setSalt(null);

        return ResponseEntity.ok(user);
    }


}
