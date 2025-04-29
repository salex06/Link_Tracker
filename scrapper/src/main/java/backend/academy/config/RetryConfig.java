package backend.academy.config;

import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.retry.HttpRetryPolicy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Getter
@Setter
@Configuration
public class RetryConfig {
    private ApplicationStabilityProperties properties;

    @Autowired
    public RetryConfig(ApplicationStabilityProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        HttpRetryPolicy retryPolicy = new HttpRetryPolicy(properties);
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(properties.getRetry().getBackoff());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
