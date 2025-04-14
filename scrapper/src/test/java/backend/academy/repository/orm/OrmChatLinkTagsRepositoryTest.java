package backend.academy.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
class OrmChatLinkTagsRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrmChatLinkTagsRepository chatLinkTagsRepository;

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
    public void deleteByChatPrimaryIdAndLinkId_WhenEntryExists_ThenDelete() {
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (5)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_tags(chat_id, link_id, tag_value) VALUES (1,1,'tag1'), (1,1,'tag2')");

        chatLinkTagsRepository.deleteByChatPrimaryIdAndLinkId(1L, 1L);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_link_tags", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void deleteByChatPrimaryIdAndLinkId_WhenNoEntry_ThenDoesNotThrow() {
        Long chatId = 1L;
        Long linkId = 2L;

        assertDoesNotThrow(() -> chatLinkTagsRepository.deleteByChatPrimaryIdAndLinkId(chatId, linkId));
    }

    @Test
    public void findTagValuesByChatPrimaryIdAndLinkIdWorksCorrectly() {
        List<String> expectedTags = List.of("tag1", "tag2");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (5)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_tags(chat_id, link_id, tag_value) VALUES (1,1,'tag1'), (1,1,'tag2')");

        List<String> tags = chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(1L, 1L);

        assertThat(tags).isNotNull();
        assertEquals(expectedTags, tags);
    }

    @Test
    public void findLinkIdsByChatIdAndTagValueWorksCorrectly() {
        List<Long> expectedLinkIds = List.of(1L, 2L);
        Long primaryChatId = 1L;
        String tagValue = "tag";
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any'), ('any2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (5)");
        jdbcTemplate.update("INSERT INTO chat_link_tags(chat_id, link_id, tag_value) VALUES (1,1,'tag'), (1,2,'tag')");

        List<Long> actualLinkIds = chatLinkTagsRepository.findLinkIdsByChatIdAndTagValue(primaryChatId, tagValue);

        assertEquals(expectedLinkIds, actualLinkIds);
    }
}
