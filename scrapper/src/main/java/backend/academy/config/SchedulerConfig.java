package backend.academy.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${bot.base-url}")
    private String botUrl;

    @Bean
    RestClient botConnectionClient() {
        return RestClient.builder().requestFactory(factory).baseUrl(botUrl).build();
    }
}
