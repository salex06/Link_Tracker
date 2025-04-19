package backend.academy.config;

import backend.academy.config.properties.UserEventsProperties;
import backend.academy.consumer.KafkaErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlerConfig {
    private final UserEventsProperties topicProperties;
    private final KafkaTemplate<Long, String> stringKafkaTemplate;

    @Bean
    public KafkaErrorHandler kafkaErrorHandler() {
        return new KafkaErrorHandler(stringKafkaTemplate, topicProperties.getDltTopic());
    }
}
