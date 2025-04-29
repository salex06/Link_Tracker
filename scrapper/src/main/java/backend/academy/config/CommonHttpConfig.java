package backend.academy.config;

import backend.academy.config.properties.ApplicationStabilityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
@RequiredArgsConstructor
public class CommonHttpConfig {
    private final ApplicationStabilityProperties properties;

    @Bean
    SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getTimeout().getReadTimeout());
        return requestFactory;
    }
}
