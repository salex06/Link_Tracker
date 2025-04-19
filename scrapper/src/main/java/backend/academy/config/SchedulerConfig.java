package backend.academy.config;

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
}
