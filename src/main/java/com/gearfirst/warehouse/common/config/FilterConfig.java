package com.gearfirst.warehouse.common.config;

import com.gearfirst.warehouse.common.context.JwtHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtHeaderFilter jwtHeaderFilter;

    @Bean
    public FilterRegistrationBean<JwtHeaderFilter> jwtHeaderFilterRegistration() {
        FilterRegistrationBean<JwtHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtHeaderFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
