package backend.academy.service.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.orm.OrmChatLink;
import backend.academy.model.orm.OrmChatLinkFilters;
import backend.academy.model.orm.OrmChatLinkIdEmbedded;
import backend.academy.model.orm.OrmChatLinkTags;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.orm.OrmChatLinkFiltersRepository;
import backend.academy.repository.orm.OrmChatLinkRepository;
import backend.academy.repository.orm.OrmChatLinkTagsRepository;
import backend.academy.repository.orm.OrmChatRepository;
import backend.academy.repository.orm.OrmLinkRepository;
import backend.academy.service.LinkService;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class OrmChatServiceTest {
    @Mock
    private OrmChatRepository chatRepository;

    @Mock
    private OrmChatLinkRepository chatLinkRepository;

    @Mock
    private OrmChatLinkTagsRepository tagsRepository;

    @Mock
    private OrmChatLinkFiltersRepository filtersRepository;

    @Mock
    private OrmLinkRepository linkRepository;

    @Mock
    private ChatMapper mapper;

    @Mock
    private LinkService linkService;

    @InjectMocks
    private OrmChatService chatService;

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
        when(chatRepository.save(any(OrmChat.class))).thenReturn(new OrmChat(expectedId, chatId));
        when(mapper.toPlainTgChat(any(OrmChat.class), anySet()))
                .thenReturn(new TgChat(expectedId, chatId, new HashSet<>()));

        TgChat chat = chatService.saveChat(chatId);

        assertThat(chat).isNotNull();
        assertEquals(chatId, chat.getChatId());
        assertEquals(expectedId, chat.getInternalId());
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
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(id, chatId)));
        when(linkService.getAllLinksByChatId(chatId)).thenReturn(expectedLinks);
        when(mapper.toPlainTgChat(any(OrmChat.class), anySet())).thenReturn(new TgChat(id, chatId, expectedLinks));

        Optional<TgChat> tgChat = chatService.getPlainTgChatByChatId(chatId);

        assertThat(tgChat).isNotEmpty();
        assertThat(tgChat.get().getInternalId()).isEqualTo(id);
        assertThat(tgChat.get().getChatId()).isEqualTo(chatId);
        assertThat(tgChat.get().getLinks()).isEqualTo(expectedLinks);
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

        verify(chatLinkRepository, times(0)).save(any(OrmChatLink.class));
    }

    @Test
    public void saveTheChatLink_WhenChatIsNotConnectedToLink_ThenSaveTheChatLink() {
        Long chatId = 1L;
        Long id = 2L;
        Set<Link> expectedLinks =
                Set.of(new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId)));
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        Link link = new Link(1L, "test_link1");
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new OrmChat(1L, 1L)));
        when(linkRepository.findByLinkValue(link.getUrl())).thenReturn(Optional.of(new OrmLink("test_link1")));

        chatService.saveTheChatLink(chat, link);

        verify(chatLinkRepository, times(1)).save(any(OrmChatLink.class));
    }

    @Test
    public void updateTags_WhenChatDoNotExist_ThenReturn() {
        Long id = 1L;
        Long chatId = 2L;
        Link link = new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId));
        Set<Link> expectedLinks = Set.of(link);
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.empty());

        chatService.updateTags(link, chat, List.of("tags"));

        verify(tagsRepository, times(0)).deleteByChatPrimaryIdAndLinkId(anyLong(), anyLong());
    }

    @Test
    public void updateFilters_WhenChatDoNotExist_ThenReturn() {
        Long id = 1L;
        Long chatId = 2L;
        Link link = new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId));
        Set<Link> expectedLinks = Set.of(link);
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.empty());

        chatService.updateFilters(link, chat, List.of("filters"));

        verify(filtersRepository, times(0)).deleteByChatPrimaryIdAndLinkId(anyLong(), anyLong());
    }

    @Test
    public void updateTags_WhenChatExists_ThenSaveTags() {
        Long id = 1L;
        Long chatId = 2L;
        Link link = new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId));
        Set<Link> expectedLinks = Set.of(link);
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        List<String> expectedTags = List.of("new_tag1", "new_tag2");
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(id, chatId)));
        when(linkRepository.findByLinkValue(link.getUrl())).thenReturn(Optional.of(new OrmLink("test_link2")));

        chatService.updateTags(link, chat, expectedTags);

        verify(tagsRepository, times(1)).deleteByChatPrimaryIdAndLinkId(anyLong(), anyLong());
        verify(tagsRepository, times(2)).save(any(OrmChatLinkTags.class));
        assertEquals(link.getTags(), expectedTags);
    }

    @Test
    public void updateFilters_WhenChatExists_ThenSaveFilters() {
        Long id = 1L;
        Long chatId = 2L;
        Link link = new Link(2L, "test_link2", List.of("tag3"), List.of("filter2"), Set.of(chatId));
        Set<Link> expectedLinks = Set.of(link);
        TgChat chat = new TgChat(id, chatId, expectedLinks);
        List<String> expectedFilters = List.of("new_filter1", "new_filter2");
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(id, chatId)));
        when(linkRepository.findByLinkValue(link.getUrl())).thenReturn(Optional.of(new OrmLink("test_link2")));

        chatService.updateFilters(link, chat, expectedFilters);

        verify(filtersRepository, times(1)).deleteByChatPrimaryIdAndLinkId(anyLong(), anyLong());
        verify(filtersRepository, times(2)).save(any(OrmChatLinkFilters.class));
        assertEquals(link.getFilters(), expectedFilters);
    }

    @Test
    public void removeTheChatLink_WhenChatDoNotExist_ThenReturn() {
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.empty());
        TgChat chat = Mockito.mock(TgChat.class);
        when(chat.getChatId()).thenReturn(1L);

        chatService.removeTheChatLink(chat, new Link(1L, "url"));

        verify(chatLinkRepository, times(0)).deleteById(any(OrmChatLinkIdEmbedded.class));
    }

    @Test
    public void removeTheChatLink_WhenChatExists_ThenRemove() {
        Long id = 1L;
        Long chatId = 2L;
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(id, chatId)));
        OrmLink ormLink = new OrmLink(1L, "url", Instant.now(), "undefined");
        Link link = new Link(1L, "url");
        when(linkRepository.findByLinkValue(link.getUrl())).thenReturn(Optional.of(ormLink));
        TgChat chat = Mockito.mock(TgChat.class);
        when(chat.getChatId()).thenReturn(chatId);

        chatService.removeTheChatLink(chat, link);

        verify(chatLinkRepository, times(1)).deleteById(any(OrmChatLinkIdEmbedded.class));
        verify(tagsRepository, times(1)).deleteByChatPrimaryIdAndLinkId(anyLong(), anyLong());
        verify(filtersRepository, times(1)).deleteByChatPrimaryIdAndLinkId(anyLong(), anyLong());
    }

    @Test
    public void getTags_WhenChatDoNotExist_ThenReturnEmptyList() {
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.empty());
        TgChat chat = Mockito.mock(TgChat.class);
        when(chat.getChatId()).thenReturn(1L);

        List<String> tags = chatService.getTags(1L, 1L);

        assertThat(tags).isEmpty();
    }

    @Test
    public void getFilters_WhenChatDoNotExist_ThenReturnEmptyList() {
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.empty());
        TgChat chat = Mockito.mock(TgChat.class);
        when(chat.getChatId()).thenReturn(1L);

        List<String> filters = chatService.getFilters(1L, 1L);

        assertThat(filters).isEmpty();
    }

    @Test
    public void getTags_WhenChatExists_ThenReturnTags() {
        Long linkId = 1L;
        Long chatId = 2L;
        List<String> expectedTags = List.of("tag1", "tag2");
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new OrmChat(chatId, 12345L)));
        when(tagsRepository.findTagValuesByChatPrimaryIdAndLinkId(chatId, linkId))
                .thenReturn(expectedTags);
        TgChat chat = Mockito.mock(TgChat.class);
        when(chat.getChatId()).thenReturn(chatId);

        List<String> tags = chatService.getTags(linkId, chatId);

        assertThat(tags).isEqualTo(expectedTags);
    }

    @Test
    public void getFilters_WhenChatExists_ThenReturnFilters() {
        Long linkId = 1L;
        Long chatId = 2L;
        List<String> expectedFilters = List.of("filter1", "filter2");
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new OrmChat(chatId, 12345L)));
        when(filtersRepository.findFilterValuesByChatIdAndLinkId(chatId, linkId))
                .thenReturn(expectedFilters);
        TgChat chat = Mockito.mock(TgChat.class);
        when(chat.getChatId()).thenReturn(chatId);

        List<String> filters = chatService.getFilters(linkId, chatId);

        assertThat(filters).isEqualTo(expectedFilters);
    }

    @Test
    public void updateTimeConfig_WhenImmediately_ThenReturnTrue() {
        TgChat chat = new TgChat(1L, 2L, new HashSet<>());
        String timeConfig = "immediately";

        boolean result = chatService.updateTimeConfig(chat, timeConfig);

        assertThat(result).isTrue();
    }

    @Test
    public void updateTimeConfig_WhenCorrectTime_ThenReturnTrue() {
        TgChat chat = new TgChat(1L, 2L, new HashSet<>());
        String timeConfig = "10:34";

        boolean result = chatService.updateTimeConfig(chat, timeConfig);

        assertThat(result).isTrue();
    }

    @Test
    public void updateTimeConfig_WhenWrongConfig_ThenReturnFalse() {
        TgChat chat = new TgChat(1L, 2L, new HashSet<>());
        String timeConfig = "25:94";

        boolean result = chatService.updateTimeConfig(chat, timeConfig);

        assertThat(result).isFalse();
    }

    @Test
    public void getChatIdsForImmediateDispatchWorksCorrectly() {
        List<Long> chatIds = List.of(1L, 2L);
        List<Long> expectedChatIds = List.of(1L);
        OrmChat chat1 = new OrmChat(10L, 1L, null);
        OrmChat chat2 = new OrmChat(20L, 2L, LocalTime.now());
        when(chatRepository.findByChatId(1L)).thenReturn(Optional.of(chat1));
        when(chatRepository.findByChatId(2L)).thenReturn(Optional.of(chat2));

        List<Long> actualChatIds = chatService.getChatIdsForImmediateDispatch(chatIds);

        assertEquals(expectedChatIds, actualChatIds);
    }

    @Test
    public void getChatIdsWithDelayedSendingWorksCorrectly() {
        LocalTime time = LocalTime.now();
        List<Long> chatIds = List.of(1L, 2L);
        List<Map.Entry<Long, LocalTime>> expectedChatIds = List.of(Map.entry(2L, time));
        OrmChat chat1 = new OrmChat(10L, 1L, null);
        OrmChat chat2 = new OrmChat(20L, 2L, time);
        when(chatRepository.findByChatId(1L)).thenReturn(Optional.of(chat1));
        when(chatRepository.findByChatId(2L)).thenReturn(Optional.of(chat2));

        List<Map.Entry<Long, LocalTime>> actualChatIds = chatService.getChatIdsWithDelayedSending(chatIds);

        assertEquals(expectedChatIds, actualChatIds);
    }
}
