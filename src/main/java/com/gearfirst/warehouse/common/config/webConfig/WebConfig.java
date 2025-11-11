package com.gearfirst.warehouse.common.config.webConfig;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 URL에 대해
                .allowedOrigins("http://localhost:5173") // 허용할 origin, 여러 개도 가능
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")   // 모든 헤더 허용
                .allowCredentials(true) // 쿠키 인증 허용
                .maxAge(3600);        // 프리플라이트 응답 캐시 시간(초)
    }
}
