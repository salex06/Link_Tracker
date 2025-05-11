package backend.academy.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.repository.jdbc.JdbcLinkRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
class JdbcLinkRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcLinkRepository linkRepository;

    private final RowMapper<JdbcLink> jdbcLinkRowMapper = (rs, rn) -> new JdbcLink(
            rs.getLong("id"),
            rs.getString("link_value"),
            rs.getTimestamp("last_update").toInstant());

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
    public void getLinkByUrl_WhenLinkExists_ThenReturnCorrectLink() {
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link')");

        Optional<JdbcLink> link = linkRepository.getLinkByUrl("test_link");

        assertThat(link).isNotEmpty();
        assertThat(link.get().getId()).isEqualTo(1L);
        assertThat(link.get().getUrl()).isEqualTo("test_link");
    }

    @Test
    public void getLinkByUrl_WhenNoLink_ThenReturnEmpty() {
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('another_test_link')");

        Optional<JdbcLink> link = linkRepository.getLinkByUrl("test_link");

        assertThat(link).isEmpty();
    }

    @Test
    public void findAll_WhenGetFirstPage_ThenReturnCorrectLinks() {
        List<String> expectedLinkValues = List.of("test_link1", "test_link2");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link2')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (2, 2), (3, 1)");
        Pageable pageable = PageRequest.of(0, 2);

        Page<JdbcLink> page = linkRepository.findAll(pageable, Duration.ofNanos(1));

        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        List<String> links = page.getContent().stream().map(JdbcLink::getUrl).toList();
        assertThat(links).isEqualTo(expectedLinkValues);
    }

    @Test
    public void findAll_WhenGetSecondPage_ThenReturnCorrectLinks() {
        List<String> expectedLinkValues = List.of("test_link3");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link2')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (2, 2), (3, 1)");
        Pageable pageable = PageRequest.of(1, 2);

        Page<JdbcLink> page = linkRepository.findAll(pageable, Duration.ofNanos(1));

        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        List<String> links = page.getContent().stream().map(JdbcLink::getUrl).toList();
        assertThat(links).isEqualTo(expectedLinkValues);
    }

    @Test
    public void findAll_WhenAnyLinkHasNoChats_ThenSkipThisOne() {
        List<String> expectedLinkValues = List.of("test_link1", "test_link3");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link2')");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link3')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (3, 1)");
        Pageable pageable = PageRequest.of(0, 2);

        Page<JdbcLink> page = linkRepository.findAll(pageable, Duration.ofNanos(1));

        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        List<String> links = page.getContent().stream().map(JdbcLink::getUrl).toList();
        assertThat(links).isEqualTo(expectedLinkValues);
    }

    @Test
    public void findAll_WhenLinkWasUpdatedRecently_ThenSkipThisOne() {
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1)");
        Pageable pageable = PageRequest.of(0, 2);

        Page<JdbcLink> page = linkRepository.findAll(pageable, Duration.ofDays(1));

        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    public void getChatIdsByUrl_WhenUrlExists_ThenReturnSetOfIds() {
        String url = "test_link1";
        Set<Long> expectedIds = Set.of(1L, 2L);
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (4)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1), (2, 1)");

        Set<Long> actualIds = linkRepository.getChatIdsByUrl(url);
        assertThat(actualIds).isEqualTo(expectedIds);
    }

    @Test
    public void getChatIdsByUrl_WhenNoUrl_ThenReturnEmptySet() {
        String url = "test_link1";
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('another_test')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (4)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1), (2, 1)");

        Set<Long> actualIds = linkRepository.getChatIdsByUrl(url);

        assertThat(actualIds).isEmpty();
    }

    @Test
    public void getLinkByUrlAndChatId_WhenLinkIsConnectedToChat_ThenReturnLink() {
        String url = "test_link1";
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1)");

        Optional<JdbcLink> actualLink = linkRepository.getLinkByUrlAndChatId(chatId, url);

        assertThat(actualLink).isNotEmpty();
        assertThat(actualLink.get().getUrl()).isEqualTo(url);
    }

    @Test
    public void getLinkByUrlAndChatId_WhenLinkIsConnectedToAnotherChat_ThenReturnEmpty() {
        String url = "test_link1";
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (2, 1)");

        Optional<JdbcLink> actualLink = linkRepository.getLinkByUrlAndChatId(chatId, url);

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void getLinkByUrlAndChatId_WhenNoLink_ThenReturnEmpty() {
        String url = "test_link1";
        Long chatId = 1L;

        Optional<JdbcLink> actualLink = linkRepository.getLinkByUrlAndChatId(chatId, url);

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void getAllLinksByChatId_WhenChatHasSomeLinks_ThenReturnListOfLinks() {
        List<String> expectedUrl = List.of("test_link1", "test_link2");
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1'), ('test_link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1), (1, 2)");

        List<JdbcLink> actualLink = linkRepository.getAllLinksByChatId(chatId);

        assertThat(actualLink.size()).isEqualTo(2);
        assertThat(actualLink.stream().map(JdbcLink::getUrl).toList()).isEqualTo(expectedUrl);
    }

    @Test
    public void getAllLinksByChatId_WhenChatHasNoLinks_ThenReturnEmptyList() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1'), ('test_link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        List<JdbcLink> actualLink = linkRepository.getAllLinksByChatId(chatId);

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void getAllLinksByChatId_WhenNoChat_ThenReturnEmptyList() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link1'), ('test_link2')");

        List<JdbcLink> actualLink = linkRepository.getAllLinksByChatId(chatId);

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void save_WhenNoLinkInDatabase_ThenInsertEntity() {
        Long expectedId = 1L;
        String expectedUrl = "url";
        JdbcLink link = new JdbcLink(null, expectedUrl);

        JdbcLink actualLink = linkRepository.save(link);

        assertThat(actualLink).isNotNull();
        assertEquals(expectedId, actualLink.getId());
        assertEquals(expectedUrl, actualLink.getUrl());
    }

    @Test
    public void save_WhenLinkInDatabase_ThenUpdateEntity() {
        Long expectedId = 2L;
        String expectedUrl = "new_url";
        Instant expectedLocalDateTime = Instant.now();
        JdbcLink link = new JdbcLink(2L, expectedUrl, expectedLocalDateTime);
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('any'), ('url')");

        JdbcLink actualLink = linkRepository.save(link);

        assertThat(actualLink).isNotNull();
        assertEquals(expectedId, actualLink.getId());
        assertEquals(expectedUrl, actualLink.getUrl());
        assertEquals(expectedLocalDateTime, actualLink.getLastUpdateTime());
    }

    @Test
    public void updateLinkWorksCorrectly() {
        Long linkId = 1L;
        String expectedUrl = "new_url";
        Instant expectedLocalDateTime = Instant.now();
        jdbcTemplate.update("INSERT INTO link(link_value, last_update) VALUES ('old_url', '1970-01-01 00:00:00')");

        linkRepository.updateLink(linkId, expectedUrl, expectedLocalDateTime);

        JdbcLink actualLink = jdbcTemplate.queryForObject("SELECT * FROM link WHERE id = 1", jdbcLinkRowMapper);
        assertNotNull(actualLink);
        assertEquals(linkId, actualLink.getId());
        assertEquals(expectedUrl, actualLink.getUrl());
        Instant expectedTruncated = expectedLocalDateTime.truncatedTo(ChronoUnit.SECONDS);
        Instant actualTruncated = actualLink.getLastUpdateTime().truncatedTo(ChronoUnit.SECONDS);
        assertEquals(expectedTruncated, actualTruncated);
    }

    @Test
    public void findAllLinkIdsByTagAndChatIdWorksCorrectly() {
        String tagValue = "expectedTag";
        Long chatId = 1L;
        List<Long> expectedLinkIds = List.of(1L, 2L);
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('link1'), ('link2'), ('link3')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(link_id, tg_chat_id) VALUES (1, 1), (1, 2), (2, 1)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_tags(link_id, chat_id, tag_value) VALUES (1, 1, 'expectedTag'), (1, 2, 'tag'), (2, 1, 'expectedTag')");

        List<Long> actualLinkIds = linkRepository.findAllLinkIdsByTagAndChatId(chatId, tagValue);

        assertNotNull(actualLinkIds);
        assertEquals(expectedLinkIds, actualLinkIds);
    }

    @Test
    public void getLinkById_WhenNoLinkInDb_ThenReturnEmpty() {
        Long linkId = 4L;

        Optional<JdbcLink> actualLink = linkRepository.getLinkById(linkId);

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void getLinkById_WhenLinkInDb_ThenReturnLink() {
        Long linkId = 1L;
        String linkValue = "link";
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('link')");

        Optional<JdbcLink> actualLink = linkRepository.getLinkById(linkId);

        assertThat(actualLink).isNotEmpty();
        assertThat(actualLink.orElseThrow().getId()).isEqualTo(linkId);
        assertThat(actualLink.orElseThrow().getUrl()).isEqualTo(linkValue);
    }
}
