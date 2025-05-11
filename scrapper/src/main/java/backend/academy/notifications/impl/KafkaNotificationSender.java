package backend.academy.notifications.impl;

import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.NotificationSender;
import backend.academy.notifications.fallback.FallbackSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "Kafka")
@Slf4j
public class KafkaNotificationSender implements NotificationSender {
    private final KafkaTemplate<Long, LinkUpdate> template;
    private final FallbackSender fallbackSender;

    @Value("${app.user-events.topic}")
    private String topic;

    @Override
    public String send(LinkUpdate update) {
        log.atInfo()
                .setMessage("Отправка уведомления об обновлении в Kafka")
                .addKeyValue("url", update.url())
                .addKeyValue("description", update.description())
                .addKeyValue("tg-chat-ids", update.tgChatIds())
                .log();
        try {
            template.send(topic, update);
            return "OK";
        } catch (Exception e) {
            log.atInfo()
                    .setMessage("Ошибка отправка уведомления об обновлении в Kafka. Переключение на резервный sender")
                    .addKeyValue("url", update.url())
                    .addKeyValue("description", update.description())
                    .addKeyValue("tg-chat-ids", update.tgChatIds())
                    .log();
            fallbackSender.send(update);
            return "Kafka exception";
        }
    }
}
