package backend.academy.config;

import backend.academy.clients.ClientManager;
import backend.academy.notifications.NotificationSender;
import backend.academy.notifications.impl.HttpNotificationSender;
import backend.academy.scheduler.Scheduler;
import backend.academy.service.LinkService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@EnableScheduling
@Configuration
public class SchedulerConfig {
    @Bean
    RestClient botConnectionClient() {
        return RestClient.builder().baseUrl("http://localhost:8080").build();
    }

    @Bean
    public HttpNotificationSender httpNotificationSender(@Qualifier("botConnectionClient") RestClient client) {
        return new HttpNotificationSender(client);
    }

    @Bean
    public Scheduler scheduler(
            LinkService linkService,
            ClientManager clientManager,
            @Qualifier("httpNotificationSender") NotificationSender notificationSender) {
        return new Scheduler(linkService, clientManager, notificationSender);
    }
}
