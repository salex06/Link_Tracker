package backend.academy.config.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ConfigurationProperties("resilience4j.retry.instances.default")
public class RetryDefaultProperties {
    private Integer maxAttempts;
    private Integer waitDuration;
}
