package com.gearfirst.warehouse.common.config.webConfig;

import com.gearfirst.warehouse.common.resolver.CurrentUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
    //    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**") // 모든 URL에 대해
//                .allowedOrigins("http://localhost:5173") // 허용할 origin, 여러 개도 가능
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
//                .allowedHeaders("*")   // 모든 헤더 허용
//                .allowCredentials(true) // 쿠키 인증 허용
//                .maxAge(3600);        // 프리플라이트 응답 캐시 시간(초)
//    }
}
