package backend.academy.api;

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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@Controller
public class LinkController {
    private final ChatService chatService;
    private final LinkService linkService;
    private final ClientManager clientManager;

    @Autowired
    public LinkController(ChatService chatService, LinkService linkService, ClientManager clientManager) {
        this.chatService = chatService;
        this.linkService = linkService;
        this.clientManager = clientManager;
    }

    /**
     * Эндпоинт, обрабатывающий запрос на получение всех отслеживаемых ссылок пользователем с идентификатором
     * {@code chatId}
     *
     * @param chatId идентификатор чата
     * @return {@code ResponseEntity<>} - ответ в случае успеха. Иначе возвращается
     *     {@code ResponseEntity<ApiErrorResponse>}
     */
    @GetMapping("/links")
    ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.atInfo()
                .setMessage("Запрос на получение отслеживаемых ссылок")
                .addKeyValue("chat-id", chatId)
                .log();
        Set<Link> chatLinks = chatService.getAllLinksByChatId(chatId);
        if (chatLinks != null) {
            ListLinksResponse response =
                    new ListLinksResponse(chatLinks.stream().toList(), chatLinks.size());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        log.atError().setMessage("Чат не найден").addKeyValue("chat-id", chatId).log();

        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Эндпоинт, обрабатывающий запрос на добавление (регистрацию) ссылки
     *
     * @param chatId идентификатор чата
     * @param addLinkRequest объект передачи данных, хранящий информацию о ссылке
     * @return {@code ResponseEntity<>} - ответ в случае успеха. Иначе возвращается
     *     {@code ResponseEntity<ApiErrorResponse>}
     */
    @PostMapping("/links")
    ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest addLinkRequest) {
        log.atInfo()
                .setMessage("Запрос на отслеживание ссылки")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("link", addLinkRequest.link())
                .log();
        Optional<TgChat> optChat = chatService.getChatByChatId(chatId);
        String linkUrl = addLinkRequest.link();
        if (optChat.isPresent() && linkService.validateLink(clientManager.availableClients(), linkUrl)) {
            Link link = chatService
                    .getLink(chatId, linkUrl)
                    .orElseGet(() -> chatService.saveLink(chatId, new Link(null, addLinkRequest.link())));
            TgChat chat = optChat.orElseThrow();
            List<String> tags = addLinkRequest.tags();
            List<String> filters = addLinkRequest.filters();
            if (!tags.isEmpty()) chatService.updateTags(link, chat, tags);
            if (!filters.isEmpty()) chatService.updateFilters(link, chat, filters);
            chatService.saveTheChatLink(chat, link);
            return new ResponseEntity<>(
                    new LinkResponse(link.getId(), link.getUrl(), addLinkRequest.tags(), addLinkRequest.filters()),
                    HttpStatus.OK);
        }

        log.atError()
                .setMessage("Некорректные параметры запроса на отслеживание ссылки")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("link", addLinkRequest.link())
                .log();

        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Эндпоинт для обработки запросов на прекращение отслеживания ссылки
     *
     * @param chatId идентификатор чата
     * @param request объект передачи данных, хранящий информацию о ссылке
     * @return {@code ResponseEntity<>} - ответ в случае успеха. Иначе возвращается
     *     {@code ResponseEntity<ApiErrorResponse>}
     */
    @DeleteMapping("/links")
    ResponseEntity<?> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        log.atInfo()
                .setMessage("Запрос на удаление ссылки")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("link", request.link())
                .log();
        Optional<TgChat> optChat = chatService.getChatByChatId(chatId);
        String url = request.link();
        Optional<Link> optLink = chatService.getLink(chatId, url);
        if (optChat.isPresent() && optLink.isPresent()) {
            TgChat chat = optChat.orElseThrow();
            Link link = optLink.orElseThrow();
            List<String> tags = chatService.getTags(link, chat);
            List<String> filters = chatService.getFilters(link, chat);
            chatService.removeTheChatLink(chat, link);
            return new ResponseEntity<>(new LinkResponse(link.getId(), link.getUrl(), tags, filters), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }
}
