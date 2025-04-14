package backend.academy.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.orm.OrmLink;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrmChatLinkRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrmChatLinkRepository chatLinkRepository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("link_tracker")
            .withUsername("postgres")
            .withPassword("123");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
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
    public void findAllChatIdByLinkIdWorksCorrectly() {
        Long linkId = 1L;
        Set<Long> expectedIds = Set.of(1L, 2L);
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1), (2, 1)");

        Set<Long> actualChatIds = chatLinkRepository.findAllChatIdByLinkId(linkId);

        assertNotNull(actualChatIds);
        assertEquals(expectedIds, actualChatIds);
    }

    @Test
    public void findAllByChatPrimaryIdWorksCorrectly() {
        List<OrmLink> expectedLinks =
                List.of(new OrmLink(1L, "url1", Instant.now()), new OrmLink(2L, "url2", Instant.now()));
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('url1'), ('url2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1), (1, 2)");

        List<OrmLink> actualLinks = chatLinkRepository.findAllByChatPrimaryId(chatId);

        assertEquals(expectedLinks, actualLinks);
    }

    @Test
    public void findByChatPrimaryIdAndLinkValueWorksCorrectly() {
        Long chatId = 1L;
        String linkValue = "url1";
        OrmLink expectedLink = new OrmLink(1L, linkValue, Instant.now());
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('url1'), ('url2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1), (1, 2)");

        Optional<OrmLink> actualLink = chatLinkRepository.findByChatPrimaryIdAndLinkValue(chatId, linkValue);

        assertThat(actualLink).isNotEmpty();
        assertEquals(expectedLink, actualLink.orElseThrow());
    }
}
