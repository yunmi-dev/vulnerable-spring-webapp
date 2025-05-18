package com.securitydemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 취약점: 모든 출처에서의 CORS 요청 허용
        registry.addMapping("/**")
                .allowedOriginPatterns("*")    // 취약점: 모든 출처 허용
                .allowedMethods("*")    // 취약점: 모든 HTTP 메서드 허용
                .allowedHeaders("*")    // 취약점: 모든 헤더 허용
                .allowCredentials(true);    // 취약점: 자격 증명 포함 허용
    }
}
