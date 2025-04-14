package backend.academy.notifications.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import backend.academy.dto.LinkUpdate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@DirtiesContext
@Testcontainers
public class KafkaNotificationSenderTest {
    @Container
    private static final KafkaContainer kafkaContainer =
            new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);

    @Autowired
    private KafkaNotificationSender kafkaSender;

    private static KafkaConsumer<Long, LinkUpdate> consumer;
    private static final String topicName = "notifications-from-resources";

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("app.user-events.replicas", () -> 1);
    }

    @BeforeAll
    static void setup() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.LongDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);
        props.put(
                org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "backend.academy.dto");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicName));
    }

    @Test
    public void sendWorksCorrectly() {
        Long expectedId = 1L;
        String expectedUrl = "url";
        String expectedDescription = "description";
        List<Long> expectedTgChatIds = List.of(1L, 2L);
        LinkUpdate expectedLinkUpdate = new LinkUpdate(expectedId, expectedUrl, expectedDescription, expectedTgChatIds);

        kafkaSender.send(expectedLinkUpdate);

        ConsumerRecords<Long, LinkUpdate> records = consumer.poll(Duration.ofSeconds(20));
        assertThat(records.count()).isGreaterThan(0);
        ConsumerRecord<Long, LinkUpdate> record = records.iterator().next();
        LinkUpdate actualLinkUpdate = record.value();
        assertEquals(expectedLinkUpdate, actualLinkUpdate);
        consumer.commitSync();
    }
}
