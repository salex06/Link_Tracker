package backend.academy.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.orm.OrmChat;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrmChatRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrmChatRepository chatRepository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("link_tracker")
            .withUsername("postgres")
            .withPassword("123");

    @BeforeAll
    static void beforeAll() throws SQLException {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @BeforeAll
    static void migrateDatabase() throws Exception {
        try (Connection connection = postgres.createConnection("")) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase("migrations/master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при выполнении миграций Liquibase", e);
        }
    }

    @BeforeEach
    public void resetTable() {
        jdbcTemplate.execute("TRUNCATE TABLE link RESTART IDENTITY CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE tg_chat RESTART IDENTITY CASCADE;");
        jdbcTemplate.execute("ALTER SEQUENCE link_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE tg_chat_id_seq RESTART WITH 1");
    }

    @Test
    public void findByChatId_WhenChatNotFound_ThenReturnEmpty() {
        Long tgChatId = 12345L;

        Optional<OrmChat> actualChat = chatRepository.findByChatId(tgChatId);

        assertThat(actualChat).isEmpty();
    }

    @Test
    public void findByChatId_WhenChatWasFound_ThenReturnChat() {
        Long tgChatId = 12345L;
        OrmChat expectedChat = new OrmChat(1L, tgChatId);
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (12345)");

        Optional<OrmChat> actualChat = chatRepository.findByChatId(tgChatId);

        assertThat(actualChat).isNotEmpty();
        assertEquals(expectedChat, actualChat.orElseThrow());
    }

    @Test
    public void existsByChatId_WhenChatDoesNotExist_ThenReturnFalse() {
        Long tgChatId = 12345L;

        boolean result = chatRepository.existsByChatId(tgChatId);

        assertThat(result).isFalse();
    }

    @Test
    public void existsByChatId_WhenChatExists_ThenReturnTrue() {
        Long tgChatId = 12345L;
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (12345)");

        boolean result = chatRepository.existsByChatId(tgChatId);

        assertThat(result).isTrue();
    }

    @Test
    public void deleteByChatIdWorksCorrectly() {
        Long tgChatId = 12345L;
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (12345)");

        chatRepository.deleteByChatId(tgChatId);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tg_chat", Integer.class);
        assertThat(count).isEqualTo(0);
    }
}
