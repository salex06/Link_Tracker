package backend.academy.notifications;

import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.fallback.FallbackSender;
import backend.academy.notifications.fallback.impl.HttpFallbackSender;
import backend.academy.notifications.fallback.impl.KafkaFallbackSender;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class FallbackBeans {
    private final RestClient botConnectionClient;
    private final KafkaTemplate<Long, LinkUpdate> kafkaTemplate;
    private final ApplicationStabilityProperties applicationStabilityProperties;

    public FallbackBeans(
            @Qualifier("botConnectionClient") RestClient botConnectionClient,
            KafkaTemplate<Long, LinkUpdate> kafkaTemplate,
            ApplicationStabilityProperties applicationStabilityProperties) {
        this.botConnectionClient = botConnectionClient;
        this.kafkaTemplate = kafkaTemplate;
        this.applicationStabilityProperties = applicationStabilityProperties;
    }

    @Bean
    public FallbackSender fallbackSender(@Value("${app.message-transport-fallback}") String fallbackTransportName) {
        if (Objects.equals(fallbackTransportName, "Kafka")) {
            return new KafkaFallbackSender(kafkaTemplate);
        } else {
            return new HttpFallbackSender(botConnectionClient, applicationStabilityProperties);
        }
    }
}
