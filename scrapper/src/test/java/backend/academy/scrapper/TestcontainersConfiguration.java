package backend.academy.scrapper;

import java.sql.Connection;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

// isolated from the "bot" module's containers!
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
                .withExposedPorts(5432)
                .withDatabaseName("local")
                .withUsername("postgres")
                .withPassword("test");
        postgres.start();
        try (Connection connection = postgres.createConnection("")) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase("migrations/master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update();
        } catch (SQLException | DatabaseException e) {
            throw new RuntimeException("Ошибка при выполнении миграций Liquibase", e);
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
        return postgres;
    }

    @Bean
    @RestartScope
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
    }
}
