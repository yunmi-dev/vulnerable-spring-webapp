package com.securitydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// Spring Security 자동설정 비활성화하여 취약한 환경 구성함
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class VulnerableWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(VulnerableWebappApplication.class, args);
	}

}
