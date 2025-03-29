package backend.academy.api;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.model.plain.Link;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
