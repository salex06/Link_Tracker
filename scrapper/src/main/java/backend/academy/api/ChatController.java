package backend.academy.api;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.service.ChatService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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
        if (!chatService.deleteChat(id)) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Чат не существует", "404", "", "", new ArrayList<>()), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                new ApiErrorResponse("Чат успешно удален", "200", "", "", new ArrayList<>()), HttpStatus.OK);
    }
}
