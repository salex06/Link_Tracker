package backend.academy.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.model.Link;
import backend.academy.model.TgChat;
import backend.academy.repository.impl.MapLinkRepository;
import backend.academy.repository.impl.MapTgChatRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class LinkControllerTest {
    private LinkController linkController;
    private LinkService linkService;
    private ChatService chatService;

    private Client mockedClient;
    private ClientManager clientManager;

    @BeforeEach
    public void setup() {
        mockedClient = Mockito.mock(Client.class);
        clientManager = Mockito.mock(ClientManager.class);
        when(clientManager.availableClients()).thenReturn(List.of(mockedClient));
        when(mockedClient.supportLink(anyString())).thenReturn(true);
        linkService = Mockito.mock(LinkService.class);
        chatService = Mockito.mock(ChatService.class);

        linkController = new LinkController(chatService, linkService);
    }

    @Test
    public void getLinks_WhenCorrectRequest_ThenReturnSuccessMessage() {
        Set<Link> expectedSet = Set.of(new Link("test1"), new Link("test2"));
        ListLinksResponse expected = new ListLinksResponse(expectedSet.stream().toList(), expectedSet.size());
        when(chatService.getChatLinks(anyLong())).thenReturn(expectedSet);

        ResponseEntity<?> response = linkController.getLinks(1L);
        ListLinksResponse actualList = ((ListLinksResponse) response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expected, actualList);
    }

    @Test
    public void getLinks_WhenWrongRequest_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.getChatLinks(anyLong())).thenReturn(null);

        ResponseEntity<?> response = linkController.getLinks(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void addLink_WhenNoRegisteredChat_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.getChat(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = linkController.addLink(1L, new AddLinkRequest("test", null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void addLink_WhenCorrectRequest_ThenReturnSuccessMessage() {
        LinkResponse expectedResponse = new LinkResponse(1L, "test", null, null);
        when(chatService.getChat(1L)).thenReturn(Optional.of(new TgChat(1L, new HashSet<>())));
        when(linkService.saveOrGetLink(any(Link.class))).thenReturn(new Link(1L, "test"));
        when(linkService.validateLink(anyString())).thenReturn(true);

        ResponseEntity<?> actual = linkController.addLink(1L, new AddLinkRequest("test", null, null));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isInstanceOf(LinkResponse.class);
        assertThat(((LinkResponse) actual.getBody()).url()).isEqualTo(expectedResponse.url());
        assertThat(((LinkResponse) actual.getBody()).id()).isEqualTo(expectedResponse.id());
    }

    @Test
    public void addLink_WhenTheLinkWasAddedInDB_ThenReturnSuccess() {
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        tgChatRepository.save(1L);
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        Long chatId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest("test", null, null);

        linkController.addLink(chatId, addLinkRequest);
        Optional<TgChat> chat = tgChatRepository.getById(chatId);
        List<Link> links = linkRepository.getAllLinks();

        // чат зарегистрирован в БД
        assertThat(chat).isNotEqualTo(Optional.empty());
        // чат отслеживает необходимые ссылки
        assertThat(chat.get().links()).isEqualTo(Set.of(new Link(1L, "test")));
        // хранилище ссылок содержит зарегистрированную ссылку
        assertThat(links).contains(new Link(1L, "test"));
    }

    @Test
    public void addLink_WhenTheLinkHasBeenAlreadyAddedByOneUser_ThenReturnTheOldLink() {
        Long chatId = 1L;
        AddLinkRequest request = new AddLinkRequest("test", null, null);
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        tgChatRepository.save(1L);
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        linkController.addLink(chatId, request);

        linkController.addLink(chatId, request);
        Optional<TgChat> chat = tgChatRepository.getById(chatId);
        List<Link> links = linkRepository.getAllLinks();

        // чат зарегистрирован в БД
        assertThat(chat).isNotEqualTo(Optional.empty());
        // чат отслеживает только одну ссылку
        assertThat(chat.get().links().size()).isEqualTo(1);
        // чат отслеживает корректную ссылку
        assertThat(chat.get().links()).isEqualTo(Set.of(new Link("test")));

        // в БД только одна ссылка (нет дубля)
        assertThat(links.size()).isEqualTo(1);
        // в БД содержится корректная ссылка
        assertThat(links).contains(new Link(1L, "test"));
    }

    @Test
    public void addLink_WhenTheLinkHasBeenAlreadyAddedByAnotherPeople_ThenReturnTheOldLink() {
        Long chat1Id = 1L;
        Long chat2Id = 2L;
        AddLinkRequest request = new AddLinkRequest("test", null, null);
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        tgChatRepository.save(1L);
        tgChatRepository.save(2L);
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        linkController.addLink(chat1Id, request);

        linkController.addLink(chat2Id, request);
        Optional<TgChat> chat1 = tgChatRepository.getById(chat1Id);
        Optional<TgChat> chat2 = tgChatRepository.getById(chat2Id);
        List<Link> links = linkRepository.getAllLinks();

        // чат 1 зарегистрирован в БД
        assertThat(chat1).isNotEqualTo(Optional.empty());
        // чат 1 отслеживает только одну ссылку
        assertThat(chat1.get().links().size()).isEqualTo(1);
        // чат 1 отслеживает корректную ссылку
        assertThat(chat1.get().links()).isEqualTo(Set.of(new Link("test")));

        // чат 2 зарегистрирован в БД
        assertThat(chat2).isNotEqualTo(Optional.empty());
        // чат 2 отслеживает только одну ссылку
        assertThat(chat2.get().links().size()).isEqualTo(1);
        // чат 2 отслеживает корректную ссылку
        assertThat(chat2.get().links()).isEqualTo(Set.of(new Link("test")));

        // в БД только одна ссылка (нет дубля)
        assertThat(links.size()).isEqualTo(1);
        // в БД содержится корректная ссылка
        assertThat(links).contains(new Link(1L, "test"));
    }

    @Test
    public void addLink_WhenAddTwoLinks_ThenSaveTheseLinks() {
        Long chat1Id = 1L;
        AddLinkRequest request1 = new AddLinkRequest("test1", null, null);
        AddLinkRequest request2 = new AddLinkRequest("test2", null, null);
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        tgChatRepository.save(1L);
        tgChatRepository.save(2L);
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));

        linkController.addLink(chat1Id, request1);
        linkController.addLink(chat1Id, request2);

        Optional<TgChat> chat1 = tgChatRepository.getById(chat1Id);
        List<Link> links = linkRepository.getAllLinks();

        // чат 1 зарегистрирован в БД
        assertThat(chat1).isNotEqualTo(Optional.empty());
        // чат 1 отслеживает только одну ссылку
        assertThat(chat1.get().links().size()).isEqualTo(2);
        // чат 1 отслеживает корректную ссылку
        assertThat(chat1.get().links()).isEqualTo(Set.of(new Link("test1"), new Link("test2")));

        // в БД только одна ссылка (нет дубля)
        assertThat(links.size()).isEqualTo(2);
        // в БД содержится корректная ссылка
        assertThat(links).contains(new Link(1L, "test1"), new Link(2L, "test2"));
    }

    @Test
    public void addLink_WhenAddLinksByDifferentUsers_ThenReturnCorrectSetOfLinks() {
        Long chat1Id = 1L;
        Long chat2Id = 2L;
        AddLinkRequest request1 = new AddLinkRequest("test1", null, null);
        AddLinkRequest request2 = new AddLinkRequest("test2", null, null);
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        tgChatRepository.save(1L);
        tgChatRepository.save(2L);
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));

        linkController.addLink(chat1Id, request1);
        linkController.addLink(chat1Id, request2);
        linkController.addLink(chat2Id, request1);

        Optional<TgChat> chat1 = tgChatRepository.getById(chat1Id);
        Optional<TgChat> chat2 = tgChatRepository.getById(chat2Id);
        List<Link> links = linkRepository.getAllLinks();

        // чат 1 зарегистрирован в БД
        assertThat(chat1).isNotEqualTo(Optional.empty());
        // чат 1 отслеживает две ссылки
        assertThat(chat1.get().links().size()).isEqualTo(2);
        // чат 1 отслеживает корректные ссылки
        assertThat(chat1.get().links()).isEqualTo(Set.of(new Link("test1"), new Link("test2")));

        // чат 2 зарегистрирован в БД
        assertThat(chat2).isNotEqualTo(Optional.empty());
        // чат 2 отслеживает только одну ссылку
        assertThat(chat2.get().links().size()).isEqualTo(1);
        // чат 2 отслеживает корректную ссылку
        assertThat(chat2.get().links()).isEqualTo(Set.of(new Link("test1")));

        // в БД только две ссылки (нет дубля)
        assertThat(links.size()).isEqualTo(2);
        // в БД содержатся корректные ссылки
        assertThat(links).contains(new Link(1L, "test1"), new Link(2L, "test2"));
    }

    @Test
    public void removeLink_WhenChatIsNotRegistered_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));

        ResponseEntity<?> response = linkController.removeLink(1L, new RemoveLinkRequest("test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void removeLink_WhenLinkNotFound_ThenReturnErrorMessage() {
        String expectedMessage = "Ссылка не найдена";
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        tgChatRepository.save(1L);
        linkController.addLink(1L, new AddLinkRequest("testLink1", null, null));

        ResponseEntity<?> response = linkController.removeLink(1L, new RemoveLinkRequest("testLink"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void removeLink_WhenUserDontTrackTheLink_ThenReturnErrorMessage() {
        String expectedMessage = "Ссылка не найдена";
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        tgChatRepository.save(1L);
        linkController.addLink(1L, new AddLinkRequest("testLink", null, null));
        tgChatRepository.save(2L);

        ResponseEntity<?> response = linkController.removeLink(2L, new RemoveLinkRequest("testLink"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void removeLink_WhenCorrectRequest_ThenReturnOkMessage() {
        String expectedUrl = "testLink";
        Long expectedId = 1L;
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        tgChatRepository.save(1L);
        linkController.addLink(1L, new AddLinkRequest("testLink", null, null));

        ResponseEntity<?> response = linkController.removeLink(1L, new RemoveLinkRequest("testLink"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LinkResponse.class);
        assertThat(((LinkResponse) response.getBody()).url()).isEqualTo(expectedUrl);
    }

    @Test
    public void removeLink_WhenTheTwoUsersTrackTheLinkAndOneOfThemRemoveLink_ThenAnotherUserTracksTheLink() {
        Long chat1Id = 1L;
        Long chat2Id = 2L;
        MapLinkRepository linkRepository = new MapLinkRepository();
        MapTgChatRepository tgChatRepository = new MapTgChatRepository();
        linkController =
                new LinkController(new ChatService(tgChatRepository), new LinkService(linkRepository, clientManager));
        tgChatRepository.save(chat1Id);
        tgChatRepository.save(chat2Id);
        linkController.addLink(chat1Id, new AddLinkRequest("testLink", null, null));
        linkController.addLink(chat2Id, new AddLinkRequest("testLink", null, null));

        ResponseEntity<?> response = linkController.removeLink(chat1Id, new RemoveLinkRequest("testLink"));

        // валидация ответа
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LinkResponse.class);

        // в БД содержится ссылка
        assertThat(linkRepository.getAllLinks().size()).isEqualTo(1);
        assertThat(linkRepository.getAllLinks()).isEqualTo(List.of(new Link(1L, "testLink")));

        // пользователь 1 не отслеживает ссылки
        assertThat(tgChatRepository.getById(chat1Id).get().links()).isEmpty();

        // пользователь 2 отслеживает ссылку
        assertThat(tgChatRepository.getById(chat2Id).get().links()).isEqualTo(Set.of(new Link(1L, "testLink")));
    }
}
