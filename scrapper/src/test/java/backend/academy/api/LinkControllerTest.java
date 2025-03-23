package backend.academy.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.ArrayList;
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

        linkController = new LinkController(chatService, linkService, clientManager);
    }

    @Test
    public void getLinks_WhenCorrectRequest_ThenReturnSuccessMessage() {
        Set<Link> expectedSet = Set.of(new Link(1L, "test1"), new Link(2L, "test2"));
        ListLinksResponse expected = new ListLinksResponse(expectedSet.stream().toList(), expectedSet.size());
        when(linkService.getAllLinksByChatId(anyLong())).thenReturn(expectedSet);

        ResponseEntity<?> response = linkController.getLinks(1L);
        ListLinksResponse actualList = ((ListLinksResponse) response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(expected, actualList);
    }

    @Test
    public void getLinks_WhenWrongRequest_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(linkService.getAllLinksByChatId(anyLong())).thenReturn(null);

        ResponseEntity<?> response = linkController.getLinks(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void addLink_WhenNoRegisteredChat_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.getPlainTgChatByChatId(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = linkController.addLink(1L, new AddLinkRequest("test", null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void addLink_WhenInvalidLink_ThenReturnErrorMessage() {
        String expectedMessage = "Некорректные параметры запроса";
        when(chatService.getPlainTgChatByChatId(anyLong()))
                .thenReturn(Optional.of(new TgChat(1L, 1L, new HashSet<>())));
        when(linkService.validateLink(anyList(), anyString())).thenReturn(false);

        ResponseEntity<?> response = linkController.addLink(1L, new AddLinkRequest("test", null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void addLink_WhenCorrectRequest_ThenReturnSuccessMessage() {
        LinkResponse expectedResponse = new LinkResponse(1L, "test", null, null);
        when(chatService.getPlainTgChatByChatId(1L)).thenReturn(Optional.of(new TgChat(1L, 1L, new HashSet<>())));
        when(linkService.saveLink(any(Link.class), any(TgChat.class))).thenReturn(new Link(1L, "test"));
        when(linkService.validateLink(anyList(), anyString())).thenReturn(true);

        ResponseEntity<?> actual = linkController.addLink(1L, new AddLinkRequest("test", null, null));

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isInstanceOf(LinkResponse.class);
        assertThat(((LinkResponse) actual.getBody()).url()).isEqualTo(expectedResponse.url());
        assertThat(((LinkResponse) actual.getBody()).id()).isEqualTo(expectedResponse.id());
    }

    @Test
    public void removeLink_WhenChatIsNotRegistered_ThenReturnErrorMessage() {
        String expectedMessage = "Чат не существует";
        when(chatService.getPlainTgChatByChatId(anyLong())).thenReturn(Optional.empty());
        when(linkService.getLink(anyLong(), anyString())).thenReturn(Optional.of(new Link(1L, "test")));

        ResponseEntity<?> response = linkController.removeLink(1L, new RemoveLinkRequest("test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedMessage);
    }

    @Test
    public void removeLink_WhenLinkNotFound_ThenReturnErrorMessage() {
        ApiErrorResponse expectedResponse =
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>());
        when(chatService.getPlainTgChatByChatId(anyLong()))
                .thenReturn(Optional.of(new TgChat(1L, 2L, new HashSet<>())));

        ResponseEntity<?> response = linkController.removeLink(1L, new RemoveLinkRequest("testLink"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody())).isEqualTo(expectedResponse);
    }

    @Test
    public void removeLink_WhenCorrectRequest_ThenReturnOkMessage() {
        Link expectedLink = new Link(1L, "test_link", List.of("tag1"), List.of("filter1:filter1"), new HashSet<>());
        LinkResponse expectedResponse = new LinkResponse(
                expectedLink.getId(), expectedLink.getUrl(), expectedLink.getTags(), expectedLink.getFilters());
        when(chatService.getPlainTgChatByChatId(anyLong()))
                .thenReturn(Optional.of(new TgChat(1L, 1L, new HashSet<>())));
        when(linkService.getLink(anyLong(), anyString())).thenReturn(Optional.of(expectedLink));
        when(chatService.getTags(anyLong(), anyLong())).thenReturn(List.of("tag1"));
        when(chatService.getFilters(anyLong(), anyLong())).thenReturn(List.of("filter1:filter1"));

        ResponseEntity<?> response = linkController.removeLink(1L, new RemoveLinkRequest("test_link"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LinkResponse.class);
        assertThat(((LinkResponse) response.getBody())).isEqualTo(expectedResponse);
    }
}
