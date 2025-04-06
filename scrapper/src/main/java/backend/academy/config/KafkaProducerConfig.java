package backend.academy.config;

import backend.academy.dto.LinkUpdate;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties properties;

    @Bean
    @Primary
    public KafkaTemplate<Long, LinkUpdate> kafkaTemplate() {
        var props = properties.buildProducerProperties(null);

        // Serialization
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Partitioning
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomUserPartitioner.class);

        var factory = new DefaultKafkaProducerFactory<Long, LinkUpdate>(props);
        return new KafkaTemplate<>(factory);
    }

    @Slf4j
    public static class CustomUserPartitioner implements Partitioner {

        @Override
        public int partition(
                String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
            var userId = Optional.ofNullable(key)
                    .filter(Long.class::isInstance)
                    .map(Long.class::cast)
                    .orElse(0L);
            return (int) (userId % cluster.partitionCountForTopic(topic));
        }

        @Override
        public void close() {}

        @Override
        public void configure(Map<String, ?> configs) {}
    }
}
