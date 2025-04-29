package backend.academy.config.properties;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ConfigurationProperties("app.stability")
public class ApplicationStabilityProperties {
    @NestedConfigurationProperty
    private Timeout timeout = new Timeout();

    @NestedConfigurationProperty
    private Retry retry = new Retry();

    @Getter
    @Setter
    public static class Timeout {
        private Integer readTimeout;
        private Integer connectTimeout;
    }

    @Setter
    @Getter
    public static class Retry {
        private int maxAttempts;
        private int backoff;
        private List<Integer> httpCodes;
    }
}
