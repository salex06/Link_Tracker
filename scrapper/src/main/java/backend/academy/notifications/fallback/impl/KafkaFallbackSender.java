package backend.academy.notifications.fallback.impl;

import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.fallback.FallbackSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app", name = "message-transport-fallback", havingValue = "Kafka")
@Slf4j
public class KafkaFallbackSender implements FallbackSender {
    private final KafkaTemplate<Long, LinkUpdate> template;

    @Value("${app.user-events.topic}")
    private String topic;

    @Override
    public void send(LinkUpdate update) {
        log.atInfo()
                .setMessage("Отправка уведомления об обновлении в Kafka через KafkaFallbackSender")
                .addKeyValue("url", update.url())
                .addKeyValue("description", update.description())
                .addKeyValue("tg-chat-ids", update.tgChatIds())
                .log();
        try {
            template.send(topic, update);
        } catch (Exception e) {
            log.atInfo()
                    .setMessage("Ошибка отправка уведомления об обновлении в Kafka через KafkaFallbackSender")
                    .addKeyValue("url", update.url())
                    .addKeyValue("description", update.description())
                    .addKeyValue("tg-chat-ids", update.tgChatIds())
                    .log();
        }
    }
}
