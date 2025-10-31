package com.gearfirst.warehouse.common.config.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    /**
     * (4) DLQ (Dead Letter Queue)를 위한 에러 핸들러를 Bean으로 등록합니다. 이 핸들러는 메시지 처리에 실패하면, 지정된 횟수(3번)만큼 재시도하고, 그래도 실패하면 해당 메시지를
     * DLQ(Dead Letter Topic)로 자동 전송합니다.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {

        // 1초 간격으로 최대 3번 재시도합니다.
        FixedBackOff backOff = new FixedBackOff(1000L, 2L); // 1초 간격, 총 3번 시도 (기본 1번 + 재시도 2번)

        // DeadLetterPublishingRecoverer는 재시도에 모두 실패한 메시지를
        // 자동으로 DLQ 토픽으로 보내는 역할을 합니다.
        // 토픽 이름은 자동으로 "[원본토픽이름].DLT"로 생성됩니다.

        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), backOff);
    }

    /**
     * 위에서 정의한 errorHandler를 Kafka 리스너에 적용합니다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactory<?, ?> factory,
            DefaultErrorHandler errorHandler) {
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
