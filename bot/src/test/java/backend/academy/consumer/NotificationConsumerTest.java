package backend.academy.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.config.properties.UserEventsProperties;
import backend.academy.dto.LinkUpdate;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@SpringBootTest
@DirtiesContext
@Testcontainers
@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {
    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer("apache/kafka-native:3.8.1");

    @Autowired
    private UserEventsProperties userEventsProperties;

    @MockitoSpyBean
    private NotificationFromResourcesMessageConsumer consumer;

    @MockitoSpyBean
    @Autowired
    private KafkaTemplate<Long, LinkUpdate> linkUpdateKafkaTemplate;

    @MockitoSpyBean
    @Autowired
    private KafkaTemplate<Long, String> stringKafkaTemplate;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Test
    public void consume_WhenCorrectMessage_ThenConsumeIsSuccessful() throws InterruptedException, ExecutionException {
        LinkUpdate expectedLinkUpdate =
                new LinkUpdate(390L, "https://github.com/salex06/testrepo", "Точно всё ок", new ArrayList<>());
        String expectedMessage =
                """
            {
            	"id": 390,
            	"url": "https://github.com/salex06/testrepo",
            	"description": "Точно всё ок",
            	"tgChatIds": []
            }
            """;
        CompletableFuture<SendResult<Long, String>> future =
                stringKafkaTemplate.send(userEventsProperties.getTopic(), expectedMessage);
        var consumerRecordCaptor = ArgumentCaptor.forClass(ConsumerRecord.class);
        var acknowledgmentCaptor = ArgumentCaptor.forClass(Acknowledgment.class);

        Thread.sleep(5000);
        verify(consumer, timeout(10000).atLeastOnce())
                .consume(consumerRecordCaptor.capture(), acknowledgmentCaptor.capture());
        var capturedRecord = consumerRecordCaptor.getValue();
        assertEquals(expectedLinkUpdate, capturedRecord.value());
        SendResult<Long, String> sendResult = future.get();
        assertEquals(expectedMessage, sendResult.getProducerRecord().value());
    }

    @Test
    public void consume_WhenAnyFieldOfMessageIsNull_ThenSendToDlt() throws InterruptedException, ExecutionException {
        LinkUpdate expectedLinkUpdate =
                new LinkUpdate(null, "https://github.com/salex06/testrepo", "В DLT", new ArrayList<>());
        String expectedMessage =
                "{\"id\":null,\"url\":\"https://github.com/salex06/testrepo\",\"description\":\"В DLT\",\"tgChatIds\":[]}";
        CompletableFuture<SendResult<Long, LinkUpdate>> future =
                linkUpdateKafkaTemplate.send(userEventsProperties.getTopic(), expectedLinkUpdate);

        Thread.sleep(5000);
        verify(stringKafkaTemplate, timeout(10000)).send(userEventsProperties.getDltTopic(), expectedMessage);
        SendResult<Long, LinkUpdate> sendResult = future.get();
        assertEquals(expectedLinkUpdate, sendResult.getProducerRecord().value());
    }

    @Test
    @DirtiesContext
    public void consume_WhenParseError_ThenSendToDlt() throws InterruptedException, ExecutionException {
        String expectedMessage = "123";
        CompletableFuture<SendResult<Long, String>> future =
                stringKafkaTemplate.send(userEventsProperties.getTopic(), expectedMessage);

        Thread.sleep(5000);
        verify(stringKafkaTemplate, timeout(10000)).send(userEventsProperties.getDltTopic(), expectedMessage);
        SendResult<Long, String> sendResult = future.get();
        assertEquals(expectedMessage, sendResult.getProducerRecord().value());
    }
}
