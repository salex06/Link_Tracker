package backend.academy.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.jdbc.JdbcTgChat;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
class JdbcChatRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcChatRepository chatRepository;

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
    public void save_WhenChatNotInDatabase_ThenSaveTheChat() {
        Long expectedChatId = 123L;
        JdbcTgChat chat = new JdbcTgChat(null, expectedChatId);

        JdbcTgChat actualChat = chatRepository.save(chat);

        assertThat(actualChat.id()).isNotNull();
        assertThat(actualChat.chatId()).isEqualTo(expectedChatId);
    }

    @Test
    public void save_WhenChatInDatabase_ThenReturnThisOne() {
        JdbcTgChat expectedChat = new JdbcTgChat(1L, 123L);

        JdbcTgChat actualChat = chatRepository.save(expectedChat);

        assertEquals(expectedChat, actualChat);
    }

    @Test
    public void existsByChatId_WhenChatExists_ThenReturnTrue() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        boolean result = chatRepository.existsByChatId(chatId);

        assertThat(result).isTrue();
    }

    @Test
    public void existsByChatId_WhenChatDoNotExist_ThenReturnFalse() {
        Long chatId = 1L;

        boolean result = chatRepository.existsByChatId(chatId);

        assertThat(result).isFalse();
    }

    @Test
    public void findByChatId_WhenChatIsInTheDB_ThenReturnChat() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        Optional<JdbcTgChat> actualChat = chatRepository.findByChatId(chatId);

        assertThat(actualChat).isNotEmpty();
        assertThat(actualChat.get().chatId()).isEqualTo(chatId);
    }

    @Test
    public void findByChatId_WhenChatIsNotInTheDB_ThenReturnEmpty() {
        Long chatId = 1L;

        Optional<JdbcTgChat> actualChat = chatRepository.findByChatId(chatId);

        assertThat(actualChat).isEmpty();
    }

    @Test
    public void deleteByChatIdWorksCorrectly() {
        Long chatId = 1L;
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        chatRepository.deleteByChatId(chatId);
        Long rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tg_chat", Long.class);

        assertThat(rowCount).isEqualTo(0);
    }

    @Test
    public void getChatsByLinkWorksCorrectly() {
        Long linkId = 1L;
        List<Long> expectedChatIds = List.of(1L, 2L);
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link'), ('test_link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (2, 1), (1, 1)");

        List<JdbcTgChat> chats = chatRepository.getChatsByLink(linkId);
        List<Long> actualChatIds = chats.stream().map(JdbcTgChat::chatId).toList();

        assertEquals(expectedChatIds, actualChatIds);
    }

    @Test
    public void getTagsWorksCorrectly() {
        Long linkId = 1L;
        Long chatId = 1L;
        List<String> expectedTags = List.of("tag1", "tag2");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('some_link')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_tags(link_id, chat_id, tag_value) VALUES (1,1,'tag1'), (1,1,'tag2')");

        List<String> actualTags = chatRepository.getTags(linkId, chatId);

        assertEquals(expectedTags, actualTags);
    }

    @Test
    public void getFiltersWorksCorrectly() {
        Long linkId = 1L;
        Long chatId = 1L;
        List<String> expectedFilters = List.of("filter1:property1", "filter2:property2");
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('some_link')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_filters(link_id, chat_id, filter_value) VALUES (1,1,'filter1:property1'), (1,1,'filter2:property2')");

        List<String> actualFilters = chatRepository.getFilters(linkId, chatId);

        assertEquals(expectedFilters, actualFilters);
    }

    @Test
    public void saveChatLink_WhenChatAndLinkExistsInDb_ThenSaveEntry() {
        Long chatId = 1L;
        Long linkId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        boolean result = chatRepository.saveTheChatLink(chatId, linkId);

        assertThat(result).isTrue();
    }

    @Test
    public void saveChatLink_WhenChatOrLinkDoNotExistInDb_ThenFailSaving() {
        Long chatId = 1L;
        Long linkId = 1L;

        boolean result = chatRepository.saveTheChatLink(chatId, linkId);

        assertThat(result).isFalse();
    }

    @Test
    public void deleteChatLinkWorksCorrectly() {
        Long chatId = 1L;
        Long linkId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");
        jdbcTemplate.update("INSERT INTO tg_chat_link(tg_chat_id, link_id) VALUES (1, 1)");

        chatRepository.removeTheChatLink(chatId, linkId);
        Long rowsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tg_chat_link", Long.class);

        assertThat(rowsCount).isEqualTo(0);
    }

    @Test
    public void saveTag_WhenLinkAndChatExistAndTagDoNotExists_ThenSaveTag() {
        Long chatId = 1L;
        Long linkId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        boolean result = chatRepository.saveTag(linkId, chatId, "tagValue");

        assertThat(result).isTrue();
    }

    @Test
    public void saveTag_WhenLinkOrChatDoNotExist_ThenReturnFalse() {
        Long chatId = 1L;
        Long linkId = 1L;

        boolean result = chatRepository.saveTag(linkId, chatId, "tagValue");

        assertThat(result).isFalse();
    }

    @Test
    public void saveFilter_WhenLinkAndChatExistAndTagDoNotExists_ThenSaveFilter() {
        Long chatId = 1L;
        Long linkId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('test_link')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1)");

        boolean result = chatRepository.saveTag(linkId, chatId, "filter");

        assertThat(result).isTrue();
    }

    @Test
    public void saveFilter_WhenLinkOrChatDoNotExist_ThenReturnFalse() {
        Long chatId = 1L;
        Long linkId = 1L;

        boolean result = chatRepository.saveFilter(linkId, chatId, "filter");

        assertThat(result).isFalse();
    }

    @Test
    public void removeTagWorksCorrectly() {
        Long chatId = 1L;
        Long linkId = 1L;
        String tagValue = "tag";
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('link1'), ('link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO chat_link_tags VALUES (1, 'tag', 1), (2,'another_tag', 2)");

        chatRepository.removeTag(linkId, chatId, tagValue);

        List<String> otherTags = jdbcTemplate.queryForList("SELECT tag_value FROM chat_link_tags", String.class);

        assertThat(otherTags.size()).isEqualTo(1);
        assertThat(otherTags.getFirst()).isEqualTo("another_tag");
    }

    @Test
    public void removeFilterWorksCorrectly() {
        Long chatId = 1L;
        Long linkId = 1L;
        String filterValue = "filter";
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('link1'), ('link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO chat_link_filters VALUES (1, 'filter', 1), (2,'another_filter', 2)");

        chatRepository.removeFilter(linkId, chatId, filterValue);

        List<String> otherTags = jdbcTemplate.queryForList("SELECT filter_value FROM chat_link_filters", String.class);

        assertThat(otherTags.size()).isEqualTo(1);
        assertThat(otherTags.getFirst()).isEqualTo("another_filter");
    }

    @Test
    public void removeAllFiltersWorksCorrectly() {
        Long chatId = 1L;
        Long linkId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('link1'), ('link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update(
                "INSERT INTO chat_link_filters VALUES (1, 'filter', 1), (1,'filter2',1), (2,'another_filter', 2)");

        chatRepository.removeAllFilters(linkId, chatId);

        List<String> otherFilters =
                jdbcTemplate.queryForList("SELECT filter_value FROM chat_link_filters", String.class);

        assertThat(otherFilters.size()).isEqualTo(1);
        assertThat(otherFilters.getFirst()).isEqualTo("another_filter");
    }

    @Test
    public void removeAllTagsWorksCorrectly() {
        Long chatId = 1L;
        Long linkId = 1L;
        jdbcTemplate.update("INSERT INTO link(link_value) VALUES ('link1'), ('link2')");
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (1), (2)");
        jdbcTemplate.update("INSERT INTO chat_link_tags VALUES (1, 'tag', 1), (1,'tag2',1), (2,'another_tag', 2)");

        chatRepository.removeAllTags(linkId, chatId);

        List<String> otherTags = jdbcTemplate.queryForList("SELECT tag_value FROM chat_link_tags", String.class);

        assertThat(otherTags.size()).isEqualTo(1);
        assertThat(otherTags.getFirst()).isEqualTo("another_tag");
    }

    @Test
    public void findById_WhenChatInDb_ThenReturnChat() {
        Long expectedId = 1L;
        Long expectedChatId = 12345L;
        jdbcTemplate.update("INSERT INTO tg_chat(chat_id) VALUES (12345)");

        Optional<JdbcTgChat> chat = chatRepository.findById(expectedId);

        assertThat(chat).isNotEmpty();
        assertThat(chat.get().id()).isEqualTo(expectedId);
        assertThat(chat.get().chatId()).isEqualTo(expectedChatId);
    }

    @Test
    public void findById_WhenChatNotInDb_ThenReturnChat() {
        Long expectedId = 1L;

        Optional<JdbcTgChat> chat = chatRepository.findById(expectedId);

        assertThat(chat).isEmpty();
    }
}
