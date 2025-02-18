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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatService chatService;
    private final LinkService linkService;

    @Autowired
    public ChatController(ChatService chatService, LinkService linkService) {
        this.chatService = chatService;
        this.linkService = linkService;
    }

    @PostMapping("/tg-chat/{id}")
    ResponseEntity<?> registerChat(@PathVariable Long id) {
        if (chatService.saveChat(id)) {
            return new ResponseEntity<>("Вы зарегистрированы", HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", List.of()),
                HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/tg-chat/{id}")
    ResponseEntity<?> deleteChat(@PathVariable Long id) {
        // TODO: некорректные параметры запроса???
        if (!chatService.deleteChat(id)) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Чат не существует", "404", "", "", new ArrayList<>()), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Чат успешно удален", "200", "", "", new ArrayList<>()), HttpStatus.OK);
    }

    @GetMapping("/links")
    ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        Optional<TgChat> chat = chatService.getChat(chatId);
        if (chat.isPresent()) {
            List<Link> links = chat.orElseThrow().links();
            return new ResponseEntity<>(new ListLinksResponse(links, links.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/links")
    ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest addLinkRequest) {
        Optional<TgChat> chat = chatService.getChat(chatId);
        if (chat.isPresent()) {
            // TODO: возможно, ссылка уже существует в репо
            Link link;
            Link dbLink = linkService.findLink(new Link(0L, addLinkRequest.url()));
            if (dbLink != null) {
                link = dbLink;
            } else {
                link = new Link(addLinkRequest.url());
                linkService.saveLink(link);
            }
            List<Long> tgChatIds = link.getTgChatIds();
            tgChatIds.add(chat.orElseThrow().id());
            link.setTgChatIds(tgChatIds);
            TgChat tgChat = chat.orElseThrow();
            tgChat.addLink(link);
            return new ResponseEntity<>(new LinkResponse(link.getId(), link.getUrl()), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/links")
    ResponseEntity<?> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        Optional<TgChat> chat = chatService.getChat(chatId);
        String linkToRemove = request.link();

        if (chat.isPresent()) {
            TgChat tgChat = chat.orElseThrow();
            List<Link> links = tgChat.links();
            Optional<Link> foundLink = links.stream()
                    .filter(link -> Objects.equals(link.getUrl(), linkToRemove))
                    .findFirst();
            if (foundLink.isEmpty()) {
                return new ResponseEntity<>(
                        new ApiErrorResponse("Ссылка не найдена", "404", "", "", null), HttpStatus.NOT_FOUND);
            }
            links.remove(foundLink.orElseThrow());
            List<Long> foundLinkTgChatIds = foundLink.orElseThrow().getTgChatIds();
            foundLinkTgChatIds.remove(tgChat.id());
            foundLink.orElseThrow().setTgChatIds(foundLinkTgChatIds);
            tgChat.links(links);
            chatService.getAllChat().forEach(i -> System.out.println(i.links()));
            return new ResponseEntity<>(
                    new LinkResponse(
                            foundLink.orElseThrow().getId(),
                            foundLink.orElseThrow().getUrl()),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }
}
