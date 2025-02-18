package backend.academy.config;

import backend.academy.scheduler.Scheduler;
import backend.academy.service.LinkService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class SchedulerConfig {
    @Bean
    public Scheduler scheduler(LinkService linkService) {
        return new Scheduler(linkService);
    }
}
