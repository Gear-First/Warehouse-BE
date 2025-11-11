package com.gearfirst.warehouse.common.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Time policy configuration without touching application.yml.
 * - API I/O (Jackson): Asia/Seoul (KST), no timestamp numbers
 * - DB session (Hibernate): UTC
 */
@Configuration
public class TimezoneConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer kstJacksonCustomizer() {
        return builder -> {
            builder.timeZone(TimeZone.getTimeZone("Asia/Seoul"));
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    @Bean
    public HibernatePropertiesCustomizer hibernateUtcCustomizer() {
        return (props) -> props.put("hibernate.jdbc.time_zone", "UTC");
    }
}
