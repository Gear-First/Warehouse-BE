package com.gearfirst.warehouse;

import java.util.List;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class TestKafkaConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplateMock() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    @Primary
    public KafkaProperties kafkaProperties() {
        KafkaProperties props = new KafkaProperties();
        props.setBootstrapServers(List.of("127.0.0.1:0"));
        return props;
    }
}
