package backend.academy.service.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.jdbc.JdbcChatRepository;
import backend.academy.repository.jdbc.JdbcLinkRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class SqlLinkServiceTest {
    @Mock
    private JdbcLinkRepository linkRepository;

    @Mock
    private JdbcChatRepository chatRepository;

    @Mock
    private LinkMapper linkMapper;

    @InjectMocks
    private SqlLinkService linkService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAllLinksWorksCorrectly() {
        Long link1Id = 1L;
        String link1Url = "testLink";
        Long link2Id = 2L;
        String link2Url = "testLink2";
        Set<Long> expectedLink1TgChats = Set.of(5L, 10L);
        Set<Long> expectedLink2TgChats = Set.of(6L, 11L);
        Page<Link> expectedLinks = new PageImpl<>(List.of(
                new Link(link1Id, link1Url, null, null, expectedLink1TgChats),
                new Link(link2Id, link2Url, null, null, expectedLink2TgChats)));
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        when(linkRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(new JdbcLink(link1Id, link1Url), new JdbcLink(link2Id, link2Url))));
        when(linkRepository.getChatIdsByUrl(link1Url)).thenReturn(expectedLink1TgChats);
        when(linkRepository.getChatIdsByUrl(link2Url)).thenReturn(expectedLink2TgChats);
        when(linkMapper.toPlainLink(any(JdbcLink.class), any(), any(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    JdbcLink link = invocationOnMock.getArgument(0);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getUrl(), null, null, chatIds);
                });
        when(chatRepository.findById(anyLong())).thenReturn(Optional.of(new JdbcTgChat(1L, 1L)));

        Page<Link> actualLinks = linkService.getAllLinks(pageable);

        assertEquals(expectedLinks.getContent(), actualLinks.getContent());
    }

    @Test
    public void getLink_WhenLinkDoNotExist_ThenReturnEmpty() {
        when(linkRepository.getLinkByUrlAndChatId(anyLong(), anyString())).thenReturn(Optional.empty());

        Optional<Link> actualLink = linkService.getLink(1L, "test_link");

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void getLink_WhenLinkInDb_ThenReturnLink() {
        Long expectedChatId = 1L;
        String expectedString = "test_string";
        Long expectedLinkId = 5L;
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of();
        Set<Long> expectedChatIds = Set.of(1L, 2L);
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new JdbcTgChat(1L, 1L)));
        when(linkRepository.getLinkByUrlAndChatId(anyLong(), anyString()))
                .thenReturn(Optional.of(new JdbcLink(expectedLinkId, expectedString)));
        when(chatRepository.getTags(expectedLinkId, expectedChatId)).thenReturn(expectedTags);
        when(chatRepository.getFilters(expectedLinkId, expectedChatId)).thenReturn(expectedFilters);
        when(linkRepository.getLinkByUrl(expectedString))
                .thenReturn(Optional.of(new JdbcLink(expectedLinkId, expectedString)));
        when(chatRepository.getChatsByLink(expectedLinkId))
                .thenReturn(List.of(new JdbcTgChat(1L, 1L), new JdbcTgChat(2L, 2L)));
        when(linkMapper.toPlainLink(any(JdbcLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    JdbcLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getUrl(), tags, filters, chatIds);
                });

        Optional<Link> link = linkService.getLink(expectedChatId, expectedString);

        assertThat(link).isNotEmpty();
        assertThat(link.get().getId()).isEqualTo(expectedLinkId);
        assertThat(link.get().getUrl()).isEqualTo(expectedString);
        assertThat(link.get().getTags()).isEqualTo(expectedTags);
        assertThat(link.get().getFilters()).isEqualTo(expectedFilters);
        assertThat(link.get().getTgChatIds()).isEqualTo(expectedChatIds);
    }

    @Test
    public void saveLink_WhenNoLinkInDataBase_ThenSaveLink() {
        Long chatId = 2L;
        Long expectedLinkId = 1L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(1L, chatId, new HashSet<>());
        when(linkRepository.getLinkByUrl(expectedUrl)).thenReturn(Optional.empty());
        when(linkRepository.save(any(JdbcLink.class))).thenReturn(new JdbcLink(expectedLinkId, expectedUrl));
        when(linkMapper.toJdbcLink(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            return new JdbcLink(link.getId(), link.getUrl());
        });
        when(linkRepository.getLinkByUrlAndChatId(anyLong(), anyString())).thenReturn(Optional.empty());
        when(chatRepository.saveTheChatLink(anyLong(), anyLong())).thenReturn(true);
        when(chatRepository.getTags(anyLong(), anyLong())).thenReturn(expectedTags);
        when(chatRepository.getFilters(anyLong(), anyLong())).thenReturn(expectedFilters);
        when(chatRepository.getChatsByLink(expectedLinkId)).thenReturn(List.of(new JdbcTgChat(1L, chatId)));
        when(linkMapper.toPlainLink(any(JdbcLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    JdbcLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getUrl(), tags, filters, chatIds);
                });
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new JdbcTgChat(1L, 1L)));

        Link actualLink = linkService.saveLink(expectedLink, expectedChat);

        assertThat(actualLink.getId()).isEqualTo(expectedLinkId);
        assertThat(actualLink.getUrl()).isEqualTo(expectedUrl);
        assertThat(actualLink.getTags()).isEqualTo(expectedTags);
        assertThat(actualLink.getFilters()).isEqualTo(expectedFilters);
        assertThat(actualLink.getTgChatIds()).isEqualTo(expectedChatIds);

        verify(linkRepository, times(1)).save(any(JdbcLink.class));
        verify(chatRepository, times(1)).saveTheChatLink(anyLong(), anyLong());
    }

    @Test
    public void saveLink_WhenLinkInDataBaseAndNotConnectedToChat_ThenSaveLink() {
        Long chatId = 2L;
        Long expectedLinkId = 1L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(1L, chatId, new HashSet<>());
        when(linkRepository.getLinkByUrl(expectedUrl))
                .thenReturn(Optional.of(new JdbcLink(expectedLinkId, expectedUrl)));
        when(linkRepository.save(any(JdbcLink.class))).thenReturn(new JdbcLink(expectedLinkId, expectedUrl));
        when(linkMapper.toJdbcLink(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            return new JdbcLink(link.getId(), link.getUrl());
        });
        when(linkRepository.getLinkByUrlAndChatId(anyLong(), anyString())).thenReturn(Optional.empty());
        when(chatRepository.saveTheChatLink(anyLong(), anyLong())).thenReturn(true);
        when(chatRepository.getTags(anyLong(), anyLong())).thenReturn(expectedTags);
        when(chatRepository.getFilters(anyLong(), anyLong())).thenReturn(expectedFilters);
        when(chatRepository.getChatsByLink(anyLong())).thenReturn(List.of(new JdbcTgChat(1L, chatId)));
        when(linkMapper.toPlainLink(any(JdbcLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    JdbcLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getUrl(), tags, filters, chatIds);
                });
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new JdbcTgChat(1L, 1L)));

        Link actualLink = linkService.saveLink(expectedLink, expectedChat);

        assertThat(actualLink.getId()).isEqualTo(expectedLinkId);
        assertThat(actualLink.getUrl()).isEqualTo(expectedUrl);
        assertThat(actualLink.getTags()).isEqualTo(expectedTags);
        assertThat(actualLink.getFilters()).isEqualTo(expectedFilters);
        assertThat(actualLink.getTgChatIds()).isEqualTo(expectedChatIds);

        verify(linkRepository, times(0)).save(any(JdbcLink.class));
        verify(chatRepository, times(1)).saveTheChatLink(anyLong(), anyLong());
    }

    @Test
    public void saveLink_WhenLinkInDataBaseAndIsConnectedToChat_ThenSaveLink() {
        Long chatId = 2L;
        Long expectedLinkId = 1L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(1L, chatId, new HashSet<>());
        when(linkRepository.getLinkByUrl(expectedUrl))
                .thenReturn(Optional.of(new JdbcLink(expectedLinkId, expectedUrl)));
        when(linkRepository.save(any(JdbcLink.class))).thenReturn(new JdbcLink(expectedLinkId, expectedUrl));
        when(linkMapper.toJdbcLink(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            return new JdbcLink(link.getId(), link.getUrl());
        });
        when(linkRepository.getLinkByUrlAndChatId(anyLong(), anyString()))
                .thenReturn(Optional.of(new JdbcLink(expectedLinkId, expectedUrl)));
        when(chatRepository.saveTheChatLink(anyLong(), anyLong())).thenReturn(true);
        when(chatRepository.getTags(anyLong(), anyLong())).thenReturn(expectedTags);
        when(chatRepository.getFilters(anyLong(), anyLong())).thenReturn(expectedFilters);
        when(chatRepository.getChatsByLink(anyLong())).thenReturn(List.of(new JdbcTgChat(1L, chatId)));
        when(linkMapper.toPlainLink(any(JdbcLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    JdbcLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getUrl(), tags, filters, chatIds);
                });
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new JdbcTgChat(1L, 1L)));

        Link actualLink = linkService.saveLink(expectedLink, expectedChat);

        assertThat(actualLink.getId()).isEqualTo(expectedLinkId);
        assertThat(actualLink.getUrl()).isEqualTo(expectedUrl);
        assertThat(actualLink.getTags()).isEqualTo(expectedTags);
        assertThat(actualLink.getFilters()).isEqualTo(expectedFilters);
        assertThat(actualLink.getTgChatIds()).isEqualTo(expectedChatIds);

        verify(linkRepository, times(0)).save(any(JdbcLink.class));
        verify(chatRepository, times(0)).saveTheChatLink(anyLong(), anyLong());
    }

    @Test
    public void getAllLinksByChatIdWorksCorrectly() {
        Long chatId = 1L;
        JdbcLink firstJdbcLink = new JdbcLink(1L, "test_link1");
        JdbcLink secondJdbcLink = new JdbcLink(2L, "test_link2");
        Link firstLink = new Link(1L, "test_link1", List.of("tag1"), List.of("filter1"), Set.of(1L, 2L));
        Link secondLink = new Link(2L, "test_link2", List.of("tag2"), List.of("filter2"), Set.of(1L));
        Set<Link> expectedLinks = Set.of(firstLink, secondLink);
        when(linkRepository.getAllLinksByChatId(chatId)).thenReturn(List.of(firstJdbcLink, secondJdbcLink));
        when(chatRepository.getTags(firstJdbcLink.getId(), chatId)).thenReturn(List.of("tag1"));
        when(chatRepository.getFilters(firstJdbcLink.getId(), chatId)).thenReturn(List.of("filter1"));
        when(chatRepository.getTags(secondJdbcLink.getId(), chatId)).thenReturn(List.of("tag2"));
        when(chatRepository.getFilters(secondJdbcLink.getId(), chatId)).thenReturn(List.of("filter2"));
        when(linkRepository.getLinkByUrl(firstJdbcLink.getUrl())).thenReturn(Optional.of(firstJdbcLink));
        when(linkRepository.getLinkByUrl(secondJdbcLink.getUrl())).thenReturn(Optional.of(secondJdbcLink));
        when(chatRepository.getChatsByLink(firstJdbcLink.getId()))
                .thenReturn(List.of(new JdbcTgChat(1L, 1L), new JdbcTgChat(2L, 2L)));
        when(chatRepository.getChatsByLink(secondJdbcLink.getId())).thenReturn(List.of(new JdbcTgChat(1L, 1L)));
        when(linkMapper.toPlainLink(any(JdbcLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    JdbcLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getUrl(), tags, filters, chatIds);
                });
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new JdbcTgChat(1L, 1L)));

        Set<Link> actualLinks = linkService.getAllLinksByChatId(chatId);
        assertEquals(expectedLinks, actualLinks);
    }

    @Test
    public void getChatIdsListeningToLink_WhenNoLinkInDB_ThenReturnEmpty() {
        JdbcLink link = new JdbcLink(1L, "test_link");
        when(linkRepository.getLinkByUrl(link.getUrl())).thenReturn(Optional.empty());

        Set<Long> actualChats = linkService.getChatIdsListeningToLink(link.getUrl());

        assertThat(actualChats).isEmpty();
    }

    @Test
    public void getChatIdsListeningToLink_WhenLinkInDB_ThenReturnListOfChatIds() {
        JdbcLink link = new JdbcLink(1L, "test_link");
        List<JdbcTgChat> chats = List.of(new JdbcTgChat(1L, 5L), new JdbcTgChat(2L, 10L));
        Set<Long> expectedChats = Set.of(5L, 10L);
        when(linkRepository.getLinkByUrl(link.getUrl())).thenReturn(Optional.of(link));
        when(chatRepository.getChatsByLink(link.getId())).thenReturn(chats);

        Set<Long> actualChats = linkService.getChatIdsListeningToLink(link.getUrl());

        assertEquals(expectedChats, actualChats);
    }
}
