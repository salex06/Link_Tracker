package backend.academy.service.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.orm.OrmChatLink;
import backend.academy.model.orm.OrmChatLinkIdEmbedded;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.orm.OrmChatLinkFiltersRepository;
import backend.academy.repository.orm.OrmChatLinkRepository;
import backend.academy.repository.orm.OrmChatLinkTagsRepository;
import backend.academy.repository.orm.OrmChatRepository;
import backend.academy.repository.orm.OrmLinkRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

class OrmLinkServiceTest {
    @Mock
    private OrmLinkRepository linkRepository;

    @Mock
    private OrmChatRepository chatRepository;

    @Mock
    private OrmChatLinkRepository chatLinkRepository;

    @Mock
    private OrmChatLinkTagsRepository chatLinkTagsRepository;

    @Mock
    private OrmChatLinkFiltersRepository chatLinkFiltersRepository;

    @Mock
    private LinkMapper mapper;

    @InjectMocks
    private OrmLinkService linkService;

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
        Set<Long> expectedLink1PrimaryIds = Set.of(5L, 10L);
        Set<Long> expectedLink2PrimaryIds = Set.of(6L, 11L);
        Set<OrmChat> expectedLink1TgChats = Set.of(new OrmChat(5L, 1L), new OrmChat(10L, 2L));
        Set<OrmChat> expectedLink2TgChats = Set.of(new OrmChat(6L, 3L), new OrmChat(11L, 4L));
        Page<Link> expectedLinks = new PageImpl<>(List.of(
                new Link(link1Id, link1Url, null, null, expectedLink1PrimaryIds),
                new Link(link2Id, link2Url, null, null, expectedLink2PrimaryIds)));
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        when(linkRepository.findAll(any(Instant.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(
                        new OrmLink(link1Id, link1Url, Instant.now()), new OrmLink(link2Id, link2Url, Instant.now()))));
        when(chatLinkRepository.findAllChatIdByLinkId(link1Id)).thenReturn(expectedLink1PrimaryIds);
        when(chatLinkRepository.findAllChatIdByLinkId(link2Id)).thenReturn(expectedLink2PrimaryIds);
        when(chatRepository.findAllById(expectedLink1PrimaryIds)).thenReturn(new ArrayList<>(expectedLink1TgChats));
        when(chatRepository.findAllById(expectedLink2PrimaryIds)).thenReturn(new ArrayList<>(expectedLink2TgChats));
        when(mapper.toPlainLink(any(OrmLink.class), any(), any(), anySet())).thenAnswer(invocationOnMock -> {
            OrmLink link = invocationOnMock.getArgument(0);
            Set<Long> chatIds = invocationOnMock.getArgument(3);

            return new Link(link.getId(), link.getLinkValue(), null, null, chatIds);
        });

        Page<Link> actualLinks = linkService.getAllLinks(pageable, Duration.of(10, ChronoUnit.SECONDS));

        assertEquals(expectedLinks.getContent(), actualLinks.getContent());
    }

    @Test
    public void getLink_WhenChatDoesNotExist_ThenReturnEmpty() {
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.empty());

        Optional<Link> actualLink = linkService.getLink(1L, "test_link");

        assertThat(actualLink).isEmpty();
    }

    @Test
    public void getLink_WhenLinkDoesNotExist_ThenReturnEmpty() {
        Long ormChatId = 1L;
        String linkValue = "link";
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new OrmChat(ormChatId, 2L)));
        when(chatLinkRepository.findByChatPrimaryIdAndLinkValue(ormChatId, linkValue))
                .thenReturn(Optional.empty());

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
        Set<Long> expectedPrimaryChatIds = Set.of(1L, 2L);
        Set<Long> expectedTgChatIds = Set.of(5L, 10L);
        List<OrmChat> expectedChats = List.of(new OrmChat(1L, 5L), new OrmChat(2L, 10L));
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new OrmChat(1L, 1L)));
        when(chatLinkRepository.findByChatPrimaryIdAndLinkValue(anyLong(), anyString()))
                .thenReturn(Optional.of(new OrmLink(expectedLinkId, expectedString, Instant.now())));
        when(chatLinkRepository.findAllChatIdByLinkId(expectedLinkId)).thenReturn(expectedPrimaryChatIds);
        when(chatRepository.findAllById(expectedPrimaryChatIds)).thenReturn(expectedChats);

        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(expectedChatId, expectedLinkId))
                .thenReturn(expectedTags);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(expectedChatId, expectedLinkId))
                .thenReturn(expectedFilters);
        when(mapper.toPlainLink(any(OrmLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    OrmLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getLinkValue(), tags, filters, chatIds);
                });

        Optional<Link> link = linkService.getLink(expectedChatId, expectedString);

        assertThat(link).isNotEmpty();
        assertThat(link.get().getId()).isEqualTo(expectedLinkId);
        assertThat(link.get().getUrl()).isEqualTo(expectedString);
        assertThat(link.get().getTags()).isEqualTo(expectedTags);
        assertThat(link.get().getFilters()).isEqualTo(expectedFilters);
        assertThat(link.get().getTgChatIds()).isEqualTo(expectedTgChatIds);
    }

    @Test
    public void saveLink_WhenNoChatInDb_ThenReturnNull() {
        Long chatId = 2L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(1L, chatId, new HashSet<>());
        when(chatRepository.existsByChatId(chatId)).thenReturn(false);

        Link link = linkService.saveLink(expectedLink, expectedChat);

        assertThat(link).isNull();
    }

    @Test
    public void saveLink_WhenNoLinkInDataBase_ThenSaveLink() {
        Long chatId = 2L;
        Long internalChatId = 1L;
        Long expectedLinkId = 1L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(internalChatId, chatId, new HashSet<>());
        when(chatRepository.existsById(internalChatId)).thenReturn(true);
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(internalChatId, chatId)));
        when(linkRepository.findByLinkValue(expectedUrl)).thenReturn(Optional.empty());
        when(linkRepository.save(any(OrmLink.class)))
                .thenReturn(new OrmLink(expectedLinkId, expectedUrl, Instant.now()));
        when(mapper.toOrmLink(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            return new OrmLink(link.getId(), link.getUrl(), link.getLastUpdateTime());
        });
        when(chatLinkRepository.existsById(any(OrmChatLinkIdEmbedded.class))).thenReturn(false);
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(internalChatId, expectedLinkId))
                .thenReturn(expectedTags);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(internalChatId, expectedLinkId))
                .thenReturn(expectedFilters);

        Link actualLink = linkService.saveLink(expectedLink, expectedChat);

        assertThat(actualLink.getId()).isEqualTo(expectedLinkId);
        assertThat(actualLink.getUrl()).isEqualTo(expectedUrl);
        assertThat(actualLink.getTags()).isEqualTo(expectedTags);
        assertThat(actualLink.getFilters()).isEqualTo(expectedFilters);
        assertThat(actualLink.getTgChatIds()).isEqualTo(expectedChatIds);

        verify(linkRepository, times(1)).save(any(OrmLink.class));
        verify(chatLinkRepository, times(1)).save(any(OrmChatLink.class));
    }

    @Test
    public void saveLink_WhenLinkInDataBaseAndNotConnectedToChat_ThenSaveLink() {
        Long chatId = 2L;
        Long internalChatId = 1L;
        Long expectedLinkId = 1L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(internalChatId, chatId, new HashSet<>());
        when(chatRepository.existsById(internalChatId)).thenReturn(true);
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(internalChatId, chatId)));
        when(linkRepository.findByLinkValue(expectedUrl))
                .thenReturn(Optional.of(new OrmLink(1L, expectedUrl, expectedLink.getLastUpdateTime())));
        when(linkRepository.save(any(OrmLink.class)))
                .thenReturn(new OrmLink(expectedLinkId, expectedUrl, Instant.now()));
        when(mapper.toOrmLink(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            return new OrmLink(link.getId(), link.getUrl(), link.getLastUpdateTime());
        });
        when(chatLinkRepository.existsById(any(OrmChatLinkIdEmbedded.class))).thenReturn(false);
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(internalChatId, expectedLinkId))
                .thenReturn(expectedTags);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(internalChatId, expectedLinkId))
                .thenReturn(expectedFilters);

        Link actualLink = linkService.saveLink(expectedLink, expectedChat);

        assertThat(actualLink.getId()).isEqualTo(expectedLinkId);
        assertThat(actualLink.getUrl()).isEqualTo(expectedUrl);
        assertThat(actualLink.getTags()).isEqualTo(expectedTags);
        assertThat(actualLink.getFilters()).isEqualTo(expectedFilters);
        assertThat(actualLink.getTgChatIds()).isEqualTo(expectedChatIds);

        verify(linkRepository, times(0)).save(any(OrmLink.class));
        verify(chatLinkRepository, times(1)).save(any(OrmChatLink.class));
    }

    @Test
    public void saveLink_WhenLinkInDataBaseAndIsConnectedToChat_ThenSaveLink() {
        Long chatId = 2L;
        Long internalChatId = 1L;
        Long expectedLinkId = 1L;
        String expectedUrl = "test_link";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1", "filter2");
        Set<Long> expectedChatIds = Set.of(2L);
        Link expectedLink = new Link(null, expectedUrl, expectedTags, expectedFilters, expectedChatIds);
        TgChat expectedChat = new TgChat(internalChatId, chatId, new HashSet<>());
        when(chatRepository.existsById(internalChatId)).thenReturn(true);
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(new OrmChat(internalChatId, chatId)));
        when(linkRepository.findByLinkValue(expectedUrl))
                .thenReturn(Optional.of(new OrmLink(1L, expectedUrl, expectedLink.getLastUpdateTime())));
        when(linkRepository.save(any(OrmLink.class)))
                .thenReturn(new OrmLink(expectedLinkId, expectedUrl, Instant.now()));
        when(mapper.toOrmLink(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            return new OrmLink(link.getId(), link.getUrl(), link.getLastUpdateTime());
        });
        when(chatLinkRepository.existsById(any(OrmChatLinkIdEmbedded.class))).thenReturn(true);
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(internalChatId, expectedLinkId))
                .thenReturn(expectedTags);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(internalChatId, expectedLinkId))
                .thenReturn(expectedFilters);

        Link actualLink = linkService.saveLink(expectedLink, expectedChat);

        assertThat(actualLink.getId()).isEqualTo(expectedLinkId);
        assertThat(actualLink.getUrl()).isEqualTo(expectedUrl);
        assertThat(actualLink.getTags()).isEqualTo(expectedTags);
        assertThat(actualLink.getFilters()).isEqualTo(expectedFilters);
        assertThat(actualLink.getTgChatIds()).isEqualTo(expectedChatIds);

        verify(linkRepository, times(0)).save(any(OrmLink.class));
        verify(chatLinkRepository, times(0)).save(any(OrmChatLink.class));
    }

    @Test
    public void getAllLinksByChatId_WhenChatDoesNotExist_ThenReturnEmptySet() {
        Long chatId = 1L;
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.empty());

        Set<Link> links = linkService.getAllLinksByChatId(chatId);

        assertThat(links).isEmpty();
    }

    @Test
    public void getAllLinksByChatId_WhenChatExists_ThenReturnSet() {
        Long chatId = 1L;
        Long internalId = 5L;
        OrmChat chat = new OrmChat(internalId, chatId);
        OrmLink link1 = new OrmLink(1L, "link1", Instant.now());
        OrmLink link2 = new OrmLink(2L, "link2", Instant.now());
        List<String> expectedTagsLink1 = List.of("tag1", "tag2");
        List<String> expectedTagsLink2 = List.of("tag3", "tag4");
        List<String> expectedFiltersLink1 = List.of("filter1", "filter2");
        List<String> expectedFiltersLink2 = List.of("filter3", "filter4");
        Set<Long> expectedPrimaryChatIdsLink1 = Set.of(1L, 2L);
        Set<Long> expectedPrimaryChatIdsLink2 = Set.of(1L);
        OrmChat chat1 = new OrmChat(1L, 5L);
        OrmChat chat2 = new OrmChat(2L, 10L);
        List<OrmChat> ormChatsLink1 = List.of(chat1, chat2);
        List<OrmChat> ormChatsLink2 = List.of(chat1);
        Set<Link> expectedPlainLinks = Set.of(
                new Link(
                        link1.getId(),
                        link1.getLinkValue(),
                        expectedTagsLink1,
                        expectedFiltersLink1,
                        Set.of(chat1.getChatId(), chat2.getChatId())),
                new Link(
                        link2.getId(),
                        link2.getLinkValue(),
                        expectedTagsLink2,
                        expectedFiltersLink2,
                        Set.of(chat1.getChatId())));
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(chatLinkRepository.findAllByChatPrimaryId(internalId)).thenReturn(List.of(link1, link2));
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(internalId, link1.getId()))
                .thenReturn(expectedTagsLink1);
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(internalId, link2.getId()))
                .thenReturn(expectedTagsLink2);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(internalId, link1.getId()))
                .thenReturn(expectedFiltersLink1);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(internalId, link2.getId()))
                .thenReturn(expectedFiltersLink2);
        when(chatLinkRepository.findAllChatIdByLinkId(link1.getId())).thenReturn(expectedPrimaryChatIdsLink1);
        when(chatLinkRepository.findAllChatIdByLinkId(link2.getId())).thenReturn(expectedPrimaryChatIdsLink2);
        when(chatRepository.findAllById(expectedPrimaryChatIdsLink1)).thenReturn(ormChatsLink1);
        when(chatRepository.findAllById(expectedPrimaryChatIdsLink2)).thenReturn(ormChatsLink2);
        when(mapper.toPlainLink(any(OrmLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    OrmLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getLinkValue(), tags, filters, chatIds);
                });

        Set<Link> actualLinks = linkService.getAllLinksByChatId(chatId);

        assertEquals(expectedPlainLinks, actualLinks);
    }

    @Test
    public void getChatIdsListeningToLink_WhenNoLinkInDB_ThenReturnEmpty() {
        JdbcLink link = new JdbcLink(1L, "test_link");
        when(linkRepository.findByLinkValue(link.getUrl())).thenReturn(Optional.empty());

        Set<Long> actualChats = linkService.getChatIdsListeningToLink(link.getUrl());

        assertThat(actualChats).isEmpty();
    }

    @Test
    public void getChatIdsListeningToLink_WhenLinkInDB_ThenReturnListOfChatIds() {
        OrmLink link = new OrmLink(1L, "test_link", Instant.now());
        Set<Long> primaryChatIds = Set.of(1L, 2L);
        OrmChat chat1 = new OrmChat(1L, 5L);
        OrmChat chat2 = new OrmChat(2L, 10L);
        List<OrmChat> chats = List.of(chat1, chat2);
        Set<Long> expectedChats = Set.of(5L, 10L);
        when(linkRepository.findByLinkValue(link.getLinkValue())).thenReturn(Optional.of(link));
        when(chatLinkRepository.findAllChatIdByLinkId(link.getId())).thenReturn(primaryChatIds);
        when(chatRepository.findAllById(primaryChatIds)).thenReturn(chats);

        Set<Long> actualChats = linkService.getChatIdsListeningToLink(link.getLinkValue());

        assertEquals(expectedChats, actualChats);
    }

    @Test
    public void updateLastUpdateTime_WhenLinkNotFound_ThenReturn() {
        String linkValue = "test";
        Instant updateTime = Instant.now();
        Link link = new Link(1L, linkValue);
        when(linkRepository.findByLinkValue(linkValue)).thenReturn(Optional.empty());

        linkService.updateLastUpdateTime(link, updateTime);

        verify(linkRepository, times(0)).updateLink(anyLong(), anyString(), any(Instant.class));
    }

    @Test
    public void updateLastUpdateTime_WhenLinkExists_ThenUpdateLink() {
        String linkValue = "test";
        Instant updateTime = Instant.now();
        Link link = new Link(1L, linkValue);
        OrmLink ormLink = new OrmLink(1L, linkValue, Instant.MIN);
        when(linkRepository.findByLinkValue(linkValue)).thenReturn(Optional.of(ormLink));

        linkService.updateLastUpdateTime(link, updateTime);

        verify(linkRepository, times(1)).updateLink(anyLong(), anyString(), any(Instant.class));
    }

    @Test
    public void getAllLinksByChatIdAndTagWorksCorrectly() {
        Long primaryChatId = 1L;
        String tag = "tag";
        List<Long> linkIds = List.of(1L, 2L);
        List<OrmLink> ormLinks =
                List.of(new OrmLink(1L, "link1", Instant.now()), new OrmLink(2L, "link2", Instant.now()));
        List<String> tagsLink1 = List.of("tag1", "tag");
        List<String> tagsLink2 = List.of("tag", "tag2");
        List<Link> expectedLinks = List.of(
                new Link(1L, "link1", tagsLink1, List.of(), Set.of()),
                new Link(2L, "link2", tagsLink2, List.of(), Set.of()));
        when(chatRepository.findByChatId(anyLong())).thenReturn(Optional.of(new OrmChat(1L, 2L)));
        when(chatLinkTagsRepository.findLinkIdsByChatIdAndTagValue(primaryChatId, tag))
                .thenReturn(linkIds);
        when(linkRepository.findAllById(linkIds)).thenReturn(ormLinks);
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(primaryChatId, 1L))
                .thenReturn(tagsLink1);
        when(chatLinkTagsRepository.findTagValuesByChatPrimaryIdAndLinkId(primaryChatId, 2L))
                .thenReturn(tagsLink2);
        when(chatLinkFiltersRepository.findFilterValuesByChatIdAndLinkId(anyLong(), anyLong()))
                .thenReturn(new ArrayList<>());
        when(chatLinkRepository.findAllChatIdByLinkId(anyLong())).thenReturn(Set.of(1L, 2L));
        when(mapper.toPlainLink(any(OrmLink.class), anyList(), anyList(), anySet()))
                .thenAnswer(invocationOnMock -> {
                    OrmLink link = invocationOnMock.getArgument(0);
                    List<String> tags = invocationOnMock.getArgument(1);
                    List<String> filters = invocationOnMock.getArgument(2);
                    Set<Long> chatIds = invocationOnMock.getArgument(3);

                    return new Link(link.getId(), link.getLinkValue(), tags, filters, chatIds);
                });

        List<Link> actualLinks = linkService.getAllLinksByChatIdAndTag(primaryChatId, tag);

        assertNotNull(actualLinks);
        assertEquals(expectedLinks, actualLinks);
    }
}
