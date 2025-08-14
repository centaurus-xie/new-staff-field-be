package com.example.new_staff_field_be;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NewStaffFieldBeApplicationTests {

	@Test
	void contextLoads() {
		// 确保应用上下文正确加载
		assertThat(true).isTrue();
	}

}
