package backend.academy.model.mapper.chat.impl;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StandardChatMapperTest {
    private final ChatMapper standardChatMapper = new StandardChatMapper();

    @Test
    public void toPlainChatFromJdbcWorksCorrectly() {
        Long expectedId = 1L;
        Long expectedChatId = 2L;
        Set<Link> expectedLinks = Set.of(
                new Link(1L, "1", List.of("1"), List.of("1"), Set.of(2L)),
                new Link(2L, "2", List.of("2"), List.of("2"), Set.of(2L)));
        JdbcTgChat jdbcChat = new JdbcTgChat(expectedId, expectedChatId);

        TgChat actualChat = standardChatMapper.toPlainTgChat(jdbcChat, expectedLinks);

        assertThat(actualChat).isNotNull();
        assertThat(actualChat.getInternalId()).isEqualTo((expectedId));
        assertThat(actualChat.getChatId()).isEqualTo(expectedChatId);
        assertThat(actualChat.getLinks()).isEqualTo(expectedLinks);
    }

    @Test
    public void toPlainChatFromOrmWorksCorrectly() {
        Long expectedId = 1L;
        Long expectedChatId = 2L;
        Set<Link> expectedLinks = Set.of(
                new Link(1L, "1", List.of("1"), List.of("1"), Set.of(2L)),
                new Link(2L, "2", List.of("2"), List.of("2"), Set.of(2L)));
        OrmChat ormChat = new OrmChat(expectedId, expectedChatId);

        TgChat actualChat = standardChatMapper.toPlainTgChat(ormChat, expectedLinks);

        assertThat(actualChat).isNotNull();
        assertThat(actualChat.getInternalId()).isEqualTo((expectedId));
        assertThat(actualChat.getChatId()).isEqualTo(expectedChatId);
        assertThat(actualChat.getLinks()).isEqualTo(expectedLinks);
    }

    @Test
    public void toJdbcTgChatWorksCorrectly() {
        Long expectedId = 1L;
        Long expectedChatId = 2L;
        Set<Link> links = Set.of(
                new Link(1L, "1", List.of("1"), List.of("1"), Set.of(2L)),
                new Link(2L, "2", List.of("2"), List.of("2"), Set.of(2L)));
        TgChat chat = new TgChat(expectedId, expectedChatId, links);

        JdbcTgChat actualChat = standardChatMapper.toJdbcTgChat(chat);

        assertThat(actualChat).isNotNull();
        assertThat(actualChat.getId()).isEqualTo(expectedId);
        assertThat(actualChat.getChatId()).isEqualTo(expectedChatId);
    }

    @Test
    public void toOrmTgChatWorksCorrectly() {
        Long expectedId = 1L;
        Long expectedChatId = 2L;
        Set<Link> links = Set.of(
                new Link(1L, "1", List.of("1"), List.of("1"), Set.of(2L)),
                new Link(2L, "2", List.of("2"), List.of("2"), Set.of(2L)));
        TgChat chat = new TgChat(expectedId, expectedChatId, links);

        OrmChat actualChat = standardChatMapper.toOrmChat(chat);

        assertThat(actualChat).isNotNull();
        assertThat(actualChat.getId()).isEqualTo(expectedId);
        assertThat(actualChat.getChatId()).isEqualTo(expectedChatId);
    }
}
