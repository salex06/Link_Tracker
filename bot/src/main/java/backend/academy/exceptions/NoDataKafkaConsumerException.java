package backend.academy.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Getter
@RequiredArgsConstructor
public class NoDataKafkaConsumerException extends RuntimeException {
    private final String exceptionMessage;
    private final ConsumerRecord<?, ?> data;
    private final Exception causedBy;
}
