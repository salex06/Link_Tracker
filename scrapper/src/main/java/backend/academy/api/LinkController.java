package backend.academy.api;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.model.Link;
import backend.academy.model.TgChat;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.ArrayList;
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

    @Autowired
    public LinkController(ChatService chatService, LinkService linkService) {
        this.chatService = chatService;
        this.linkService = linkService;
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
        Set<Link> chatLinks = chatService.getChatLinks(chatId);
        if (chatLinks != null) {
            return new ResponseEntity<>(
                    new ListLinksResponse(chatLinks.stream().toList(), chatLinks.size()), HttpStatus.OK);
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
        Optional<TgChat> chat = chatService.getChat(chatId);
        if (chat.isPresent() && linkService.validateLink(addLinkRequest.link())) {
            Link link = linkService.saveOrGetLink(new Link(addLinkRequest.link()));
            chatService.appendLinkToChat(chatId, link);
            linkService.appendChatToLink(chatId, link);
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
        Optional<TgChat> chat = chatService.getChat(chatId);
        if (chat.isPresent()) {
            Link foundLink = linkService.findLink(new Link(0L, request.link()));
            boolean wasDeleted = chatService.deleteLink(chatId, request.link());

            if (!wasDeleted || foundLink == null || !linkService.deleteChatFromLink(chatId, foundLink)) {
                log.atError()
                        .setMessage("Ссылка не найдена")
                        .addKeyValue("chat-id", chatId)
                        .addKeyValue("link", request.link())
                        .log();
                return new ResponseEntity<>(
                        new ApiErrorResponse("Ссылка не найдена", "404", "", "", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(
                    new LinkResponse(
                            foundLink.getId(), foundLink.getUrl(), foundLink.getTags(), foundLink.getFilters()),
                    HttpStatus.OK);
        }
        log.atError()
                .setMessage("Чат для удаления ссылки не найден")
                .addKeyValue("chat-id", chatId)
                .log();
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }
}
