package backend.academy.config;

import backend.academy.dto.LinkUpdate;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaProperties properties;

    @Bean("defaultConsumerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, LinkUpdate>>
            defaultConsumerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate>();
        factory.setConsumerFactory(consumerFactory(
                NotificationFromResourcesDeserializer.class,
                props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-consumer")));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public KafkaTemplate<Long, LinkUpdate> linkUpdateKafkaTemplate() {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        var factory = new DefaultKafkaProducerFactory<Long, LinkUpdate>(props);
        return new KafkaTemplate<>(factory);
    }

    private <M> ConsumerFactory<Long, M> consumerFactory(
            Class<? extends Deserializer<M>> valueDeserializerClass, Consumer<Map<String, Object>> propsModifier) {
        var props = properties.buildConsumerProperties(null);

        // Serialization
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);

        // Partitions
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        propsModifier.accept(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    public static class NotificationFromResourcesDeserializer extends JsonDeserializer<LinkUpdate> {}
}
