package backend.academy.config;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonKafkaConfig {

    private final NotificationFromResourcesTopicProperties notificationFromResourcesTopicProperties;
    private final KafkaProperties kafkaProperties;

    @Bean
    Admin localKafkaClusterAdminClient() {
        return AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers()));
    }

    @Bean
    KafkaAdmin localKafkaClusterAdmin() {
        return new KafkaAdmin(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers()));
    }

    @Bean
    @SneakyThrows
    KafkaAdmin.NewTopics notificationFromResourcesTopic() {
        return notificationFromResourcesTopicProperties.toNewTopics();
    }
}
