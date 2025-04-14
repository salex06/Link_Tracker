package backend.academy.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.orm.OrmChat;
import backend.academy.model.orm.OrmChatLinkFilters;
import backend.academy.model.orm.OrmLink;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
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
class OrmChatLinkFiltersRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrmChatLinkFiltersRepository ormChatLinkFiltersRepository;

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
                "INSERT INTO chat_link_filters(chat_id, link_id, filter_value) VALUES (1,1,'filter1'), (1,1,'filter2')");

        ormChatLinkFiltersRepository.deleteByChatPrimaryIdAndLinkId(1L, 1L);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_link_filters", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void deleteByChatPrimaryIdAndLinkId_WhenNoEntry_ThenDoesNotThrow() {
        Long chatId = 1L;
        Long linkId = 2L;

        assertDoesNotThrow(() -> ormChatLinkFiltersRepository.deleteByChatPrimaryIdAndLinkId(chatId, linkId));
    }

    @Test
    public void findFilterValuesByChatIdAndLinkIdReturnFilterList() {
        List<String> expectedFilters = List.of("filter1", "filter2");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (5)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_filters(chat_id, link_id, filter_value) VALUES (1,1,'filter1'), (1,1,'filter2')");

        List<String> filters = ormChatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(1L, 1L);

        assertThat(filters).isNotNull();
        assertEquals(expectedFilters, filters);
    }

    @Test
    public void saveFilter_WhenFilterIsNotInDb_ThenSave() {
        OrmChat chat = new OrmChat(1L, 5L);
        OrmLink link = new OrmLink(1L, "any", Instant.now());
        String expectedFilter = "filter1";
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (5)");

        OrmChatLinkFilters filter =
                ormChatLinkFiltersRepository.save(new OrmChatLinkFilters(chat, link, expectedFilter));

        assertNotNull(filter);
        assertEquals(chat, filter.getChat());
        assertEquals(link, filter.getLink());
        assertEquals(expectedFilter, filter.getId().getFilterValue());
    }
}
