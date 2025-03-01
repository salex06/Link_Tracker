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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class LinkController {
    private final ChatService chatService;
    private final LinkService linkService;

    @Autowired
    public LinkController(ChatService chatService, LinkService linkService) {
        this.chatService = chatService;
        this.linkService = linkService;
    }

    @GetMapping("/links")
    ResponseEntity<?> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        Set<Link> chatLinks = chatService.getChatLinks(chatId);
        if (chatLinks != null) {
            return new ResponseEntity<>(
                    new ListLinksResponse(chatLinks.stream().toList(), chatLinks.size()), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/links")
    ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest addLinkRequest) {
        Optional<TgChat> chat = chatService.getChat(chatId);
        if (chat.isPresent()) {
            Link link = linkService.saveOrGetLink(new Link(addLinkRequest.url()));
            chatService.appendLinkToChat(chatId, link);
            linkService.appendChatToLink(chatId, link);
            return new ResponseEntity<>(new LinkResponse(link.getId(), link.getUrl()), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/links")
    ResponseEntity<?> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        Optional<TgChat> chat = chatService.getChat(chatId);
        if (chat.isPresent()) {
            Link foundLink = linkService.findLink(new Link(0L, request.link()));
            boolean wasDeleted = chatService.deleteLink(chatId, request.link());

            if (!wasDeleted || foundLink == null || !linkService.deleteChatFromLink(chatId, foundLink)) {
                return new ResponseEntity<>(
                        new ApiErrorResponse("Ссылка не найдена", "404", "", "", null), HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(new LinkResponse(foundLink.getId(), foundLink.getUrl()), HttpStatus.OK);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                HttpStatus.BAD_REQUEST);
    }
}
