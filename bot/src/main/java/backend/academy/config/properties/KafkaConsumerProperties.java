package backend.academy.config.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ConfigurationProperties("spring.kafka.consumer.properties")
public class KafkaConsumerProperties {
    private String groupId;
}
