package backend.academy.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
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

class TagControllerTest {
    private TagController tagController;
    private LinkService linkService;
    private ChatService chatService;

    @BeforeEach
    public void setUp() {
        linkService = Mockito.mock(LinkService.class);
        chatService = Mockito.mock(ChatService.class);

        tagController = new TagController(linkService, chatService);
    }

    @Test
    public void getLinksByTag_WhenChatDoesNotExist_TheReturnErrorResponse() {
        ApiErrorResponse expectedResponse =
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>());
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        Long chatId = 50L;
        String tagValue = "tag";
        when(chatService.containsChat(chatId)).thenReturn(false);

        ResponseEntity<?> response = tagController.getLinksByTag(chatId, tagValue);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedResponse.description());
    }

    @Test
    public void getLinksByTag_WhenChatExists_TheReturnOkResponse() {
        List<Link> expectedLinks = List.of(
                new Link(1L, "test1", List.of("tag", "tag2"), List.of(), Set.of(1L)),
                new Link(2L, "test2", List.of("tag", "tag1", "tag3"), List.of(), Set.of(1L, 2L)));
        ListLinksResponse expectedResponse = new ListLinksResponse(expectedLinks, expectedLinks.size());
        HttpStatus expectedStatus = HttpStatus.OK;
        Long chatId = 50L;
        String tagValue = "tag";
        when(chatService.containsChat(chatId)).thenReturn(true);
        when(linkService.getAllLinksByChatIdAndTag(chatId, tagValue)).thenReturn(expectedLinks);

        ResponseEntity<?> response = tagController.getLinksByTag(chatId, tagValue);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isInstanceOf(ListLinksResponse.class);
        assertThat(((ListLinksResponse) response.getBody())).isEqualTo(expectedResponse);
    }

    @Test
    public void addTag_WhenChatDoesNotExist_ThenReturnErrorResponse() {
        Long chatId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest("url", List.of("tag1"), List.of());
        ApiErrorResponse expectedResponse =
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>());
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        when(chatService.getPlainTgChatByChatId(chatId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = tagController.addTag(false, chatId, addLinkRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedResponse.description());
    }

    @Test
    public void addTag_WhenMulticast_ThenSaveTagToAllLinks() {
        Long chatId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest("url", List.of("tag1"), List.of());
        LinkResponse expectedResponse = new LinkResponse(null, null, null, null);
        HttpStatus expectedStatus = HttpStatus.OK;
        when(chatService.getPlainTgChatByChatId(chatId)).thenReturn(Optional.of(new TgChat(1L, 3L, new HashSet<>())));

        ResponseEntity<?> response = tagController.addTag(true, chatId, addLinkRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isInstanceOf(LinkResponse.class);
        assertThat(((LinkResponse) response.getBody())).isEqualTo(expectedResponse);
    }

    @Test
    public void addTag_WhenNotMulticastAndLinkNotFound_ThenReturnError() {
        Long chatId = 1L;
        AddLinkRequest addLinkRequest = new AddLinkRequest("url", List.of("tag1"), List.of());
        ApiErrorResponse expectedResponse =
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>());
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        when(chatService.getPlainTgChatByChatId(chatId)).thenReturn(Optional.of(new TgChat(1L, 3L, new HashSet<>())));

        ResponseEntity<?> response = tagController.addTag(false, chatId, addLinkRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
        assertThat(((ApiErrorResponse) response.getBody()).description()).isEqualTo(expectedResponse.description());
    }

    @Test
    public void addTag_WhenNotMulticastAndLinkWasFound_ThenSaveTagToLink() {
        Long chatId = 1L;
        Long tgChatId = 3L;
        TgChat chat = new TgChat(chatId, tgChatId, new HashSet<>());
        List<String> tags = List.of("tag3", "t", "tag1");
        Link link = new Link(5L, "url", List.of("tag3", "t"), List.of("f1:p1"), Set.of(1L));
        AddLinkRequest addLinkRequest = new AddLinkRequest("url", List.of("tag1"), List.of());
        LinkResponse expectedResponse = new LinkResponse(5L, "url", List.of("tag3", "t", "tag1"), List.of("f1:p1"));
        HttpStatus expectedStatus = HttpStatus.OK;
        when(chatService.getPlainTgChatByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkService.getLink(tgChatId, addLinkRequest.link())).thenReturn(Optional.of(link));
        doAnswer(i -> {
                    List<String> oldTags = new ArrayList<>(link.getTags());
                    oldTags.add("tag1");
                    link.setTags(oldTags);
                    return null;
                })
                .when(chatService)
                .updateTags(link, chat, tags);

        ResponseEntity<?> response = tagController.addTag(false, chatId, addLinkRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isInstanceOf(LinkResponse.class);
        assertThat(((LinkResponse) response.getBody())).isEqualTo(expectedResponse);
    }
}
