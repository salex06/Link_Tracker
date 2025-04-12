package backend.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class RedisCacheServiceTest {
    @Container
    private static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisCacheService redisCacheService;

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> redis.getHost());
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Test
    public void invalidateCacheWorksCorrectly() {
        String key = "list:";
        redisTemplate.opsForValue().set(key + "tag1:25", "test1");
        redisTemplate.opsForValue().set(key + "tag2:13", "test2");

        redisCacheService.invalidateCache();

        Set<String> keys = redisTemplate.keys(key + "*");
        assertThat(keys).isEmpty();
    }

    @Test
    public void getValue_WhenValueExists_ThenReturnValue() {
        String key = "list:tag1:25";
        String expectedMessage = "test1";
        redisTemplate.opsForValue().set(key, expectedMessage);

        String result = redisCacheService.getValue(key);

        assertNotNull(result);
        assertEquals(expectedMessage, result);
    }

    @Test
    public void getValue_WhenValueDoesNotExist_ThenReturnNull() {
        String otherKey = "key";
        redisTemplate.opsForValue().set(otherKey, "other_test");
        String key = "list:tag1:25";

        String result = redisCacheService.getValue(key);

        assertNull(result);
    }

    @Test
    public void putValueWorksCorrectly() {
        String key = "list:tag1:25";
        String expectedMessage = "value";

        redisCacheService.putValue(key, expectedMessage);

        String cachedMessage = redisTemplate.opsForValue().get(key);
        assertNotNull(cachedMessage);
        assertEquals(expectedMessage, cachedMessage);
    }

    @AfterEach
    public void clearCache() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) redisTemplate.delete(keys);
    }
}
