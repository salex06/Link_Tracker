package backend.academy.config;

import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {
    private final KafkaProperties properties;

    @Bean
    public KafkaTemplate<Long, LinkUpdate> linkUpdateKafkaTemplate() {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        var factory = new DefaultKafkaProducerFactory<Long, LinkUpdate>(props);
        return new KafkaTemplate<>(factory);
    }

    @Bean
    public KafkaTemplate<Long, String> stringKafkaTemplate() {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        var factory = new DefaultKafkaProducerFactory<Long, String>(props);
        return new KafkaTemplate<>(factory);
    }
}
