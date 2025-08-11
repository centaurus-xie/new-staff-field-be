package com.example.new_staff_field_be.repository;

import com.example.new_staff_field_be.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
    UserLogin findByUsername(String username);
}
