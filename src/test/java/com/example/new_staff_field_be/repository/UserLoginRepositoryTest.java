package com.example.new_staff_field_be.repository;

import com.example.new_staff_field_be.entity.UserLogin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserLoginRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserLoginRepository userLoginRepository;

    @Test
    void findByUsername_ReturnsUser_WhenUserExists() {
        // 创建测试用户
        UserLogin user = new UserLogin();
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setSalt("salt123");
        entityManager.persistAndFlush(user);

        // 测试查找
        UserLogin found = userLoginRepository.findByUsername("testuser");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
        assertThat(found.getPassword()).isEqualTo("hashedpassword");
        assertThat(found.getSalt()).isEqualTo("salt123");
    }

    @Test
    void findByUsername_ReturnsNull_WhenUserNotExists() {
        UserLogin found = userLoginRepository.findByUsername("nonexistent");

        assertThat(found).isNull();
    }
}
