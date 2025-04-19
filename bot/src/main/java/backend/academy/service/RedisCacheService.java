package backend.academy.service;

import backend.academy.handler.impl.ListMessageHandler;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCacheService {
    private final RedisTemplate<String, String> redisTemplate;

    public void invalidateCache() {
        String pattern = ListMessageHandler.LIST_CACHE_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void putValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
}
