package backend.academy.repository.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.orm.OrmLink;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class OrmLinkRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrmLinkRepository linkRepository;

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
    public void findAll_WhenGetFirstPage_ThenReturnCorrectLinks() {
        List<String> expectedLinkValues = List.of("test_link1", "test_link2");
        Instant cutoff = Instant.now().plus(1, ChronoUnit.DAYS);
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link2')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (2, 2), (3, 1)");
        Pageable pageable = PageRequest.of(0, 2);

        Page<OrmLink> page = linkRepository.findAll(cutoff, pageable);

        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        List<String> links =
                page.getContent().stream().map(OrmLink::getLinkValue).toList();
        assertThat(links).isEqualTo(expectedLinkValues);
    }

    @Test
    public void findAll_WhenGetSecondPage_ThenReturnCorrectLinks() {
        List<String> expectedLinkValues = List.of("test_link3");
        Instant cutoff = Instant.now().plus(1, ChronoUnit.DAYS);
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link2')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (2, 2), (3, 1)");
        Pageable pageable = PageRequest.of(1, 2);

        Page<OrmLink> page = linkRepository.findAll(cutoff, pageable);

        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        List<String> links =
                page.getContent().stream().map(OrmLink::getLinkValue).toList();
        assertThat(links).isEqualTo(expectedLinkValues);
    }

    @Test
    public void findAll_WhenAnyLinkHasNoChats_ThenSkipThisOne() {
        List<String> expectedLinkValues = List.of("test_link1", "test_link3");
        Instant cutoff = Instant.now().plus(1, ChronoUnit.DAYS);
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link2')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (3, 1)");
        Pageable pageable = PageRequest.of(0, 2);

        Page<OrmLink> page = linkRepository.findAll(cutoff, pageable);

        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        List<String> links =
                page.getContent().stream().map(OrmLink::getLinkValue).toList();
        assertThat(links).isEqualTo(expectedLinkValues);
    }

    @Test
    public void updateLinkWorksCorrectly() {
        Long linkId = 1L;
        String expectedValue = "new_test_link";
        Instant expectedNewInstant = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");

        linkRepository.updateLink(linkId, expectedValue, expectedNewInstant);

        OrmLink link = jdbcTemplate.queryForObject(
                "SELECT * FROM link WHERE id = 1",
                (rs, rn) -> new OrmLink(
                        rs.getLong("id"),
                        rs.getString("link_value"),
                        rs.getTimestamp("last_update").toInstant()));

        assertThat(link).isNotNull();
        assertThat(link.getLinkValue()).isEqualTo(expectedValue);
        assertThat(link.getLastUpdate()).isEqualTo(expectedNewInstant);
    }
}
