package com.gearfirst.warehouse.common.config;

import com.gearfirst.warehouse.api.parts.service.PartCarModelReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoOpPartCarModelReaderConfig {

    @Bean
    public PartCarModelReader partCarModelReader() {
        return partId -> 0L; // default: no mappings
    }
}
