
package com.example.new_staff_field_be.controller;

        import com.example.new_staff_field_be.entity.UserLogin;
        import com.example.new_staff_field_be.repository.UserLoginRepository;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

        import java.security.SecureRandom;
        import java.util.Base64;
        import java.util.concurrent.ConcurrentHashMap;
        // 导入盐值工具类
        import com.example.new_staff_field_be.util.PasswordUtils;



@RestController
@CrossOrigin(origins = "http://localhost:5173") // 允许前端地址跨域
@RequestMapping("/api/auth")
public class UserLoginController {

    @Autowired
    private UserLoginRepository userLoginRepository;
    /**
     * 检查用户名是否已存在
     * @param username 待检查的用户名
     * @return true=已存在，false=可注册
     */
    @GetMapping("/checkUserName")
    public ResponseEntity<Boolean> checkUserName(@RequestParam String username) {
        System.out.println("Checking username: " + username);
        boolean exists = userLoginRepository.findByUsername(username) != null;
        return ResponseEntity.ok(exists); // 直接返回布尔值，前端根据true/false判断
    }
    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserLogin userLogin) {
        String username = userLogin.getUsername();
        String frontendEncryptedPassword = userLogin.getPassword();

        // 验证前端是否已获取盐值（从缓存中获取）
        String cachedSalt = tempSaltCache.get(username);
        if (cachedSalt == null) {
            return new ResponseEntity<>("Please get salt first (call /salt endpoint)", HttpStatus.BAD_REQUEST);
        }

        // 使用缓存盐值进行二次加密（而非重新生成）
        String finalEncryptedPassword = PasswordUtils.secondaryEncrypt(frontendEncryptedPassword, cachedSalt);
        userLogin.setPassword(finalEncryptedPassword);
        userLogin.setSalt(cachedSalt); // 存入缓存的盐值（确保与前端使用的一致）

        // 保存用户并清除缓存
        userLoginRepository.save(userLogin);
        tempSaltCache.remove(username); // 注册成功后清除缓存

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
    // 用户登陆
    @PostMapping("/login")
    public ResponseEntity<UserLogin> loginUser(@RequestBody UserLogin userLogin) {
        String username = userLogin.getUsername();
        String frontendEncryptedPassword = userLogin.getPassword(); // 前端加密后的密码

        // 1. 查询用户信息
        UserLogin existingUser = userLoginRepository.findByUsername(username);
        if (existingUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 用户名不存在
        }

        // 2. 使用存储的盐值进行二次加密验证
        String salt = existingUser.getSalt();
        String encryptedPasswordToCheck = PasswordUtils.secondaryEncrypt(frontendEncryptedPassword, salt);

        // 3. 对比二次加密后的密码与数据库存储的密码
        if (!encryptedPasswordToCheck.equals(existingUser.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 密码错误
        }

        // 4. 返回用户信息（注意：避免返回密码和盐值等敏感信息）
        existingUser.setPassword(null); // 清除密码
        existingUser.setSalt(null); // 清除盐值
        return new ResponseEntity<>(existingUser, HttpStatus.OK);// 返回200状态码和用户信息
    }



    /**
     * 为指定用户名生成盐值（无论用户是否已存在，用于前端加密密码）
     * @param username 用户名（前端传入）
     * @return 生成的盐值
     */
    private final ConcurrentHashMap<String, String> tempSaltCache = new ConcurrentHashMap<>();
    @GetMapping("/salt")
    public ResponseEntity<String> generateSaltForUser(@RequestParam String username) {
        String salt = PasswordUtils.generateSalt(username);
        tempSaltCache.put(username, salt); // 缓存盐值（有效期可设为5分钟）
        return ResponseEntity.ok(salt);
    }



}
