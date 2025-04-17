package backend.academy.notifications.impl;

import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "Kafka")
public class KafkaNotificationSender implements NotificationSender {
    private final KafkaTemplate<Long, LinkUpdate> template;

    @Value("${app.user-events.topic}")
    private String topic;

    @Override
    public void send(LinkUpdate update) {
        template.send(topic, update);
    }
}
