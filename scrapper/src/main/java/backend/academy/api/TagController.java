package backend.academy.api;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TagController {
    private final LinkService linkService;
    private final ChatService chatService;

    public TagController(LinkService linkService, ChatService chatService) {
        this.linkService = linkService;
        this.chatService = chatService;
    }

    @RateLimiter(name = "default")
    @GetMapping("/linksbytag")
    public ResponseEntity<?> getLinksByTag(
            @RequestHeader("Tg-Chat-Id") Long id, @RequestHeader("Tag-Value") String tag) {
        if (!chatService.containsChat(id)) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                    HttpStatus.BAD_REQUEST);
        }

        List<Link> links = linkService.getAllLinksByChatIdAndTag(id, tag);
        return new ResponseEntity<>(new ListLinksResponse(links, links.size()), HttpStatus.OK);
    }

    @RateLimiter(name = "default")
    @PostMapping("/addtag")
    public ResponseEntity<?> addTag(
            @RequestHeader("Add-To-All") Boolean isMulticast,
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestBody AddLinkRequest request) {
        String linkUrl = request.link();

        Optional<TgChat> chat = chatService.getPlainTgChatByChatId(chatId);
        if (chat.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                    HttpStatus.BAD_REQUEST);
        }
        TgChat tgChat = chat.orElseThrow();

        if (isMulticast) {
            chatService.addTagsToAllLinksByChatId(tgChat, request.tags());
            return new ResponseEntity<>(new LinkResponse(null, null, null, null), HttpStatus.OK);
        }

        Optional<Link> link = linkService.getLink(tgChat.getChatId(), linkUrl);
        if (link.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                    HttpStatus.BAD_REQUEST);
        }
        Link plainLink = link.orElseThrow();

        List<String> newTags = new ArrayList<>(plainLink.getTags());
        newTags.add(request.tags().getFirst());

        chatService.updateTags(plainLink, tgChat, newTags);
        return new ResponseEntity<>(
                new LinkResponse(plainLink.getId(), plainLink.getUrl(), plainLink.getTags(), plainLink.getFilters()),
                HttpStatus.OK);
    }

    @RateLimiter(name = "default")
    @DeleteMapping("/removetag")
    public ResponseEntity<?> removeTag(
            @RequestHeader("Remove-To-All") Boolean isMulticast,
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestBody AddLinkRequest request) {
        Optional<TgChat> chat = chatService.getPlainTgChatByChatId(chatId);
        if (chat.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                    HttpStatus.BAD_REQUEST);
        }
        TgChat tgChat = chat.orElseThrow();

        if (isMulticast) {
            chatService.removeTagsToAllLinksByChatId(tgChat, request.tags());
            return new ResponseEntity<>(new LinkResponse(null, null, null, null), HttpStatus.OK);
        }

        String linkUrl = request.link();
        Optional<Link> link = linkService.getLink(tgChat.getChatId(), linkUrl);
        if (link.isEmpty()) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                    HttpStatus.BAD_REQUEST);
        }
        Link plainLink = link.orElseThrow();

        List<String> newTags = new ArrayList<>(plainLink.getTags());
        newTags.remove(request.tags().getFirst());

        chatService.updateTags(plainLink, tgChat, newTags);

        return new ResponseEntity<>(
                new LinkResponse(plainLink.getId(), plainLink.getUrl(), plainLink.getTags(), plainLink.getFilters()),
                HttpStatus.OK);
    }
}
