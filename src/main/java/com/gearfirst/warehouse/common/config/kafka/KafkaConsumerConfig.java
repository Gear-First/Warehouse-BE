package com.gearfirst.warehouse.common.config.kafka;

import com.gearfirst.warehouse.api.dto.TestDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaProperties properties;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestDto> testKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TestDto> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(testConsumerFactory());
        return factory;
    }

    private ConsumerFactory<String, TestDto> testConsumerFactory() {
        // 8. application.yml에서 가져온 기본 consumer 설정을 복사합니다.
        //    (bootstrap-servers 주소 등이 여기에 포함됩니다.)
        Map<String, Object> props = properties.buildConsumerProperties(null);

        // 9. [핵심] yml의 value.deserializer 설정을 무시하고
        //    새로운 JsonDeserializer를 "강제로" 설정합니다.
        return new DefaultKafkaConsumerFactory<>(
                props, // 8번에서 가져온 기본 설정
                new org.apache.kafka.common.serialization.StringDeserializer(), // Key는 String

                // Value(메시지 내용)는 OrderDto.class로만 변환하고,
                // 타입 헤더(__TypeId__)는 무시(false)합니다.
                new JsonDeserializer<>(TestDto.class, false)
        );
    }
}
