//package com.example.new_staff_field_be.controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//@RestController
//public class UserLoginController {
//    @GetMapping("/hello")
//    public String sayHello() {
//        return "Hello, World!";
//    }
//}
package com.example.new_staff_field_be.controller;

        import com.example.new_staff_field_be.entity.UserLogin;
        import com.example.new_staff_field_be.repository.UserLoginRepository;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserLoginController {

    @Autowired
    private UserLoginRepository userLoginRepository;

    // 注册用户
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserLogin userLogin) {
        // 检查用户名是否已存在
        if (userLoginRepository.findByUsername(userLogin.getUsername()) != null) {
            return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
        }

        // 保存用户信息
        userLogin.setCreatedTime(null); // 允许JPA在保存时自动设置创建时间
        userLogin.setUpdatedTime(null);
        userLoginRepository.save(userLogin);

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    // 用户登陆
    @PostMapping("/login")
    public ResponseEntity<UserLogin> loginUser(@RequestBody UserLogin userLogin) {
        // 验证用户名和密码
        UserLogin existingUser = userLoginRepository.findByUsername(userLogin.getUsername());
        if (existingUser == null || !existingUser.getPassword().equals(userLogin.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 返回用户信息（实际应用中应避免返回敏感信息，如密码）
        // 这里仅作为示例
        return new ResponseEntity<>(existingUser, HttpStatus.OK);
    }
}
