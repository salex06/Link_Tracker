package backend.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.model.Link;
import backend.academy.repository.LinkRepository;
import backend.academy.repository.impl.MapLinkRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LinkServiceTest {
    private LinkService linkService;
    private LinkRepository linkRepository;
    private ClientManager clientManager;

    @BeforeEach
    public void setup() {
        clientManager = Mockito.mock();
        linkRepository = new MapLinkRepository();
        linkService = new LinkService(linkRepository, clientManager);
    }

    @Test
    public void getAllLinksReturnsTheListOfLinks() {
        Link link1 = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());
        Link link2 = new Link(2L, "link2", List.of("tag2", "tag22"), List.of("filter2:prop2"), new HashSet<>());
        linkRepository.save(link1);
        linkRepository.save(link2);

        List<Link> actual = linkService.getAllLinks();

        assertEquals(List.of(link1, link2), actual);
    }

    @Test
    public void getAllLinks_WhenRepositoryIsEmpty_ThenReturnEmptyList() {
        List<Link> actual = linkService.getAllLinks();

        assertThat(actual).isEmpty();
    }

    @Test
    public void getLink_WhenLinkExists_ThenReturnLink() {
        Link link1 = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());
        linkRepository.save(link1);

        Optional<Link> actual = linkRepository.getById(link1.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(link1);
    }

    @Test
    public void getLink_WhenLinkNotExists_ThenReturnLink() {
        Optional<Link> actual = linkRepository.getById(1L);

        assertThat(actual).isEmpty();
    }

    @Test
    public void findLink_WhenLinkExists_ThenReturnLink() {
        Link expected = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());
        linkRepository.save(expected);
        Link toFind = new Link(0L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        Link actual = linkService.findLink(toFind);

        assertEquals(expected, actual);
    }

    @Test
    public void findLink_WhenLinkNotExists_ThenReturnNull() {
        Link toFind = new Link(0L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        Link actual = linkService.findLink(toFind);

        assertNull(actual);
    }

    @Test
    public void saveLink_WhenLinkNotInDataBaseYet_ThenReturnNull() {
        Link link = new Link(0L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        Link actual = linkService.saveLink(link);

        assertNull(actual);
    }

    @Test
    public void saveLink_WhenLinkAlreadyInDataBase_ThenReturnOldLink() {
        Link expected = new Link(1L, "link1", List.of("tag1", "tag2"), List.of("filter1:prop1"), Set.of(1L));
        linkRepository.save(expected);
        Link link = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        Link actual = linkService.saveLink(link);

        assertEquals(expected, actual);
    }

    @Test
    public void saveOrGetLink_WhenLinkNotInDataBaseYet_ThenSaveLink() {
        Link expected = new Link(1L, "link1", List.of("tag1", "tag2"), List.of("filter1:prop1"), Set.of(1L));

        Link actual = linkService.saveOrGetLink(expected);

        assertEquals(expected, actual);
    }

    @Test
    public void saveOrGetLink_WhenLinkAlreadyInDataBase_ThenReturnOldLink() {
        Link expected = new Link(1L, "link1", List.of("tag1", "tag2"), List.of("filter1:prop1"), Set.of(1L));
        linkRepository.save(expected);
        Link linkToSave = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        Link actual = linkService.saveLink(linkToSave);

        assertEquals(expected, actual);
    }

    @Test
    public void appendChatToLink_WhenLinkInDB_ThenAppendChat() {
        Long expectedChatId = 1L;
        Link link = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());
        linkRepository.save(link);

        boolean result = linkService.appendChatToLink(expectedChatId, link);

        assertThat(result).isTrue();
        assertThat(link.getTgChatIds()).isEqualTo(Set.of(1L));
    }

    @Test
    public void appendChatToLink_WhenLinkNotFound_ThenReturnFalse() {
        Link notSavedLink = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        boolean result = linkService.appendChatToLink(1L, notSavedLink);

        assertThat(result).isFalse();
    }

    @Test
    public void deleteChatToLink_WhenChatInLinkAndLinkInDatabase_ThenReturnTrue() {
        Long expectedChatId = 1L;
        Set<Long> chatSet = new HashSet<>();
        chatSet.add(expectedChatId);
        Link savedLink = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), chatSet);
        linkService.saveLink(savedLink);

        boolean result = linkService.deleteChatFromLink(expectedChatId, savedLink);

        assertThat(result).isTrue();
        assertThat(savedLink.getTgChatIds()).isEmpty();
    }

    @Test
    public void deleteChatToLink_WhenChatNotInLinkAndLinkInDataBase_ThenReturnFalse() {
        Link savedLink = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());
        linkService.saveLink(savedLink);

        boolean result = linkService.deleteChatFromLink(1L, savedLink);

        assertThat(result).isFalse();
    }

    @Test
    public void deleteChatToLink_WhenChatNotInLinkAndLinkNotInDataBase_ThenReturnFalse() {
        Link notSavedLink = new Link(1L, "link1", List.of("tag1", "tag12"), List.of("filter1:prop1"), new HashSet<>());

        boolean result = linkService.deleteChatFromLink(1L, notSavedLink);

        assertThat(result).isFalse();
    }

    @Test
    public void validateLink_WhenLinkIsIncorrect_ThenReturnFalse() {
        String wrongLink = "abc";
        Client mockedClient = Mockito.mock(Client.class);
        when(mockedClient.supportLink(any(String.class))).thenReturn(false);
        List<Client> mockedClients = List.of(mockedClient);
        when(clientManager.availableClients()).thenReturn(mockedClients);

        boolean result = linkService.validateLink(wrongLink);

        assertThat(result).isFalse();
    }

    @Test
    public void validateLink_WhenLinkIsCorrect_ThenReturnTrue() {
        String wrongLink = "abc";
        Client mockedClient = Mockito.mock(Client.class);
        when(mockedClient.supportLink(any(String.class))).thenReturn(true);
        List<Client> mockedClients = List.of(mockedClient);
        when(clientManager.availableClients()).thenReturn(mockedClients);

        boolean result = linkService.validateLink(wrongLink);

        assertThat(result).isTrue();
    }
}
