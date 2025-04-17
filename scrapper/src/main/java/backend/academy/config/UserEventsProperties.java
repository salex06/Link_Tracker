package backend.academy.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Getter
@Setter
@EqualsAndHashCode
@Configuration
@ConfigurationProperties("app.user-events")
public class UserEventsProperties {
    private String topic;
    private int partitions;
    private short replicas;

    public KafkaAdmin.NewTopics toNewTopics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic(topic, partitions, replicas), new NewTopic(topic + "-dlt", partitions, replicas));
    }
}
