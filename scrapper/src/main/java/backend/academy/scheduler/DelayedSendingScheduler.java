package backend.academy.scheduler;

import backend.academy.dto.LinkUpdate;
import backend.academy.notifications.NotificationSender;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DelayedSendingScheduler {
    private final RedisTemplate<String, LinkUpdate> redisTemplate;
    private final NotificationSender notificationSender;

    @Scheduled(cron = "0 * * * * *")
    public void schedule() {
        String key = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Set<LinkUpdate> updateSet = redisTemplate.opsForSet().members(key);
        if (updateSet == null || updateSet.isEmpty()) {
            return;
        }

        sendUpdates(updateSet);

        redisTemplate.delete(key);
    }

    private void sendUpdates(Set<LinkUpdate> updateSet) {
        updateSet.forEach(notificationSender::send);
    }
}
