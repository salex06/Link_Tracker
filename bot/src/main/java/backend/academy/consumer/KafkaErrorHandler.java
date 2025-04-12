package backend.academy.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
public class KafkaErrorHandler implements CommonErrorHandler {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final String dltTopic;

    public KafkaErrorHandler(KafkaTemplate<Long, String> kafkaTemplate, String dltTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.dltTopic = dltTopic;
    }

    @Override
    public boolean handleOne(
            Exception thrownException,
            ConsumerRecord<?, ?> record,
            Consumer<?, ?> consumer,
            MessageListenerContainer container) {
        handle(thrownException, record, consumer);
        return true;
    }

    private void handle(Exception exception, ConsumerRecord<?, ?> data, Consumer<?, ?> consumer) {
        log.atWarn()
                .setMessage("Ошибка при обработке сообщения из-за проблем с десериализацией, отправляем в DLT")
                .addKeyValue("data", data)
                .addKeyValue("exception", exception.getMessage())
                .log();
        try {
            byte[] valueBytes;

            if (data.value() != null) {
                valueBytes = jsonMapper.writeValueAsBytes(data.value());
            } else if (data.headers().lastHeader("original_message") != null) {
                valueBytes = data.headers().lastHeader("original_message").value();
            } else {
                throw new RuntimeException("Нет данных для отправки в dlt");
            }

            String value = new String(valueBytes, StandardCharsets.UTF_8);

            Long key = (Long) data.key();
            kafkaTemplate.send(dltTopic, key, value);

        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в DLT: {}", e.getMessage());
        } finally {
            if (consumer != null && data != null) {
                try {
                    consumer.commitSync(Map.of(
                            new TopicPartition(data.topic(), data.partition()),
                            new OffsetAndMetadata(data.offset() + 1)));
                } catch (Exception commitException) {
                    log.error("Ошибка при подтверждении смещения: {}", commitException.getMessage());
                }
            } else {
                log.warn("Consumer или record is null, не можем подтвердить смещение.");
            }
        }
    }
}
