package backend.academy.consumer;

import backend.academy.bot.Bot;
import backend.academy.dto.LinkUpdate;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationFromResourcesMessageConsumer {
    private final Bot bot;

    @KafkaListener(
            containerFactory = "defaultConsumerFactory",
            topicPartitions =
                    @TopicPartition(
                            topic = "${app.user-events.topic}",
                            partitions = {"0"}))
    public void consume(ConsumerRecord<Long, LinkUpdate> record, Acknowledgment acknowledgment) {
        log.info(
                """
            Получено следующее сообщение из топика {}:
            key: {},
            value: {}
            """,
                record.topic(),
                record.key(),
                record.value());

        LinkUpdate linkUpdate = record.value();
        if (LinkUpdate.anyFieldIsNull(linkUpdate)) {
            log.atWarn()
                    .setMessage("Невалидное сообщение: одно из полей - null")
                    .addKeyValue("id", linkUpdate.id())
                    .addKeyValue("url", linkUpdate.url())
                    .addKeyValue("description", linkUpdate.description())
                    .addKeyValue("tg-chat-ids", linkUpdate.tgChatIds())
                    .log();
            throw new IllegalStateException("Ошибка валидации: одно из полей LinkUpdate - null");
        }

        sendOutMessages(record.value());
        acknowledgment.acknowledge();
    }

    private void sendOutMessages(LinkUpdate linkUpdate) {
        for (Long chatId : linkUpdate.tgChatIds()) {
            String responseText = String.format(
                    "Новое уведомление от ресурса %s (ID: %d): %s",
                    linkUpdate.url(), linkUpdate.id(), linkUpdate.description());
            SendMessage message = new SendMessage(chatId, responseText);
            bot.execute(message);
        }
    }
}
