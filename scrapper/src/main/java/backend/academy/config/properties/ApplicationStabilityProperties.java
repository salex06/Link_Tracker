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
@ConfigurationProperties("app.stability")
public class ApplicationStabilityProperties {
    private Integer readTimeout;
    private Integer connectTimeout;
}
