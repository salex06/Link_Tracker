package backend.academy.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.NotificationSender;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class DelayedSendingSchedulerTest {
    @Container
    private static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @MockitoSpyBean
    @Autowired
    private RedisTemplate<String, LinkUpdate> redisTemplate;

    @MockitoBean
    private NotificationSender sender = Mockito.mock(NotificationSender.class);

    private DelayedSendingScheduler delayedSendingScheduler;

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Test
    public void schedule_WhenNoExistingValuesInRedis_ThenDoNotSendUpdate() {
        delayedSendingScheduler = new DelayedSendingScheduler(redisTemplate, sender);

        delayedSendingScheduler.schedule();

        verify(redisTemplate, times(0)).delete(any(String.class));
        verify(sender, times(0)).send(any(LinkUpdate.class));
    }

    @Test
    public void schedule_WhenSuitableValueInRedis_ThenSendUpdates() {
        delayedSendingScheduler = new DelayedSendingScheduler(redisTemplate, sender);
        LinkUpdate update = new LinkUpdate(1L, "url", "description", List.of());
        String key = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        redisTemplate.opsForSet().add(key, update);

        delayedSendingScheduler.schedule();

        verify(redisTemplate, times(1)).delete(any(String.class));
        verify(sender, times(1)).send(any(LinkUpdate.class));
    }
}
