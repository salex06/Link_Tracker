package backend.academy.service.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.JdbcChatRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SqlChatServiceTest {
    @Mock
    private JdbcChatRepository chatRepository;

    @Mock
    private SqlLinkService linkService;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private SqlChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void saveChat_WhenChatExistsInDB_ThenReturnNull() {
        Long chatId = 1L;
        when(chatRepository.existsByChatId(chatId)).thenReturn(true);

        TgChat chat = chatService.saveChat(chatId);

        assertThat(chat).isNull();
    }

    @Test
    public void saveChat_WhenChatDoNotExistInDB_ThenReturnSavedChat() {
        Long chatId = 1L;
        Long expectedId = 2L;
        when(chatRepository.existsByChatId(chatId)).thenReturn(false);
        when(chatRepository.save(any(JdbcTgChat.class))).thenReturn(new JdbcTgChat(expectedId, chatId));
        when(chatMapper.toPlainTgChat(any(JdbcTgChat.class), anySet()))
                .thenReturn(new TgChat(expectedId, chatId, new HashSet<>()));

        TgChat chat = chatService.saveChat(chatId);

        assertThat(chat).isNotNull();
        assertEquals(chatId, chat.chatId());
        assertEquals(expectedId, chat.internalId());
    }

    @Test
    public void getPlainTgChatByChatId_WhenChatIsNotPresent_ThenReturnEmpty() {
        Long chatId = 1L;
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.empty());

        Optional<TgChat> tgChat = chatService.getPlainTgChatByChatId(chatId);

        assertThat(tgChat).isEmpty();
    }

    @Test
    public void getPlainTgChatByChatId_WhenChatIsPresent_ThenReturnEmpty() {
        Long chatId = 1L;
        Long id = 2L;
        Set<Link> expectedLinks = Set.of(
                new Link(1L, "test_link1", List.of("tag1", "tag2"), List.of("filter1:filter1"), Set.of(chatId)),
                new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId)));
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new JdbcTgChat(id, chatId)));
        when(linkService.getAllLinksByChatId(chatId)).thenReturn(expectedLinks);
        when(chatMapper.toPlainTgChat(any(JdbcTgChat.class), anySet()))
                .thenReturn(new TgChat(id, chatId, expectedLinks));

        Optional<TgChat> tgChat = chatService.getPlainTgChatByChatId(chatId);

        assertThat(tgChat).isNotEmpty();
        assertThat(tgChat.get().internalId()).isEqualTo(id);
        assertThat(tgChat.get().chatId()).isEqualTo(chatId);
        assertThat(tgChat.get().links()).isEqualTo(expectedLinks);
    }

    @Test
    public void saveTheChatLink_WhenChatHasAlreadyConnectedToLink_ThenDoNothing() {
        Long chatId = 1L;
        Long id = 2L;
        Set<Link> expectedLinks = Set.of(
                new Link(1L, "test_link1", List.of("tag1", "tag2"), List.of("filter1:filter1"), Set.of(chatId)),
                new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId)));
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        Link link = new Link(1L, "test_link1");

        chatService.saveTheChatLink(chat, link);

        verify(chatRepository, times(0)).saveTheChatLink(any(), any());
    }

    @Test
    public void saveTheChatLink_WhenChatIsNotConnectedToLink_ThenSaveTheChatLink() {
        Long chatId = 1L;
        Long id = 2L;
        Set<Link> expectedLinks =
                Set.of(new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId)));
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        Link link = new Link(1L, "test_link1");

        chatService.saveTheChatLink(chat, link);

        verify(chatRepository, times(1)).saveTheChatLink(any(), any());
    }
}
