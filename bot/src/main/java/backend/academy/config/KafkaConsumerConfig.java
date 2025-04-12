package backend.academy.config;

import backend.academy.consumer.KafkaErrorHandler;
import backend.academy.dto.LinkUpdate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaProperties properties;
    private final NotificationFromResourcesTopicProperties topicProperties;

    @Bean("defaultConsumerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, LinkUpdate>>
            defaultConsumerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(
                new KafkaErrorHandler(stringKafkaTemplate(), topicProperties.getTopic() + "-dlt"));
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

    @Bean
    public KafkaTemplate<Long, String> stringKafkaTemplate() {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        var factory = new DefaultKafkaProducerFactory<Long, String>(props);
        return new KafkaTemplate<>(factory);
    }

    private ConsumerFactory<Long, LinkUpdate> consumerFactory() {
        JsonDeserializer<LinkUpdate> valueDeserializer = new JsonDeserializer<>(LinkUpdate.class, false);
        valueDeserializer.addTrustedPackages("backend.academy.dto");

        ErrorHandlingDeserializer<LinkUpdate> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(valueDeserializer);
        errorHandlingDeserializer.setFailedDeserializationFunction(this::failedDeserializationFunction);

        Map<String, Object> props = properties.buildConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-consumer");

        return new DefaultKafkaConsumerFactory<>(
                props, new ErrorHandlingDeserializer<>(new LongDeserializer()), errorHandlingDeserializer);
    }

    private LinkUpdate failedDeserializationFunction(FailedDeserializationInfo failedDeserializationInfo) {
        byte[] data = failedDeserializationInfo.getData();
        Headers headers = failedDeserializationInfo.getHeaders();
        headers.add("deserialization_failed", "true".getBytes());
        headers.add("original_message", data);
        return null;
    }
}
