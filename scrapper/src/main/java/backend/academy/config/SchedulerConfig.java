package backend.academy.config;

import backend.academy.ScrapperConfig;
import backend.academy.clients.ClientManager;
import backend.academy.notifications.NotificationSender;
import backend.academy.scheduler.Scheduler;
import backend.academy.service.LinkService;
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
    public Scheduler scheduler(
            LinkService linkService,
            ClientManager clientManager,
            NotificationSender notificationSender,
            ScrapperConfig config) {
        return new Scheduler(linkService, clientManager, notificationSender, config);
    }
}
