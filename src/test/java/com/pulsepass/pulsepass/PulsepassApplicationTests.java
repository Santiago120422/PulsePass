package com.pulsepass.pulsepass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "pulsepass.security.jwt.secret-base64=x2XCGrSYLyn/ytOPS1jxM5uNb+7nKc9/Dy6sphYoYBI=")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PulsepassApplicationTests {

	@Test
	void contextLoads() {
	}

}
