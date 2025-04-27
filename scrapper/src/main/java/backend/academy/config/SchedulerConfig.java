package backend.academy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {
    private final SimpleClientHttpRequestFactory factory;

    @Bean
    RestClient botConnectionClient() {
        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl("http://localhost:8080")
                .build();
    }
}
