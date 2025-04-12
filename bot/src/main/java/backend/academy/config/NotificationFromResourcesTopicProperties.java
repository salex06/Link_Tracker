package backend.academy.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ConfigurationProperties("app.user-events")
public class NotificationFromResourcesTopicProperties {
    private String topic;
}
