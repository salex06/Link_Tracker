package backend.academy.config.properties;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import java.time.Duration;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ConfigurationProperties("resilience4j.circuitbreaker.instances.default")
public class CircuitBreakerDefaultProperties {
    private SlidingWindowType slidingWindowType;
    private Integer minimumNumberOfCalls;
    private Integer slidingWindowSize;
    private Integer failureRateThreshold;
    private Duration waitDurationInOpenState;
    private Integer permittedNumberOfCallsInHalfOpenState;
    private List<Exception> recordExceptions;
}
