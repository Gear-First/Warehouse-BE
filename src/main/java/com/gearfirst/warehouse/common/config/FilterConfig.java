package com.gearfirst.warehouse.common.config;

import com.gearfirst.warehouse.common.context.JwtHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtHeaderFilter jwtHeaderFilter; //  스프링 Bean 주입받음

    @Bean
    public FilterRegistrationBean<JwtHeaderFilter> jwtHeaderFilterRegistration() {
        FilterRegistrationBean<JwtHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtHeaderFilter); //  Bean 사용
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
