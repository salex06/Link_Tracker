package backend.academy.api;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ChatController {
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Эндпоинт для регистрации чата в БД
     *
     * @param id идентификатор телеграмм чата
     * @return {@code ResponseEntity<>} - ответ в случае успеха. Иначе возвращается
     *     {@code ResponseEntity<ApiErrorResponse>}
     */
    @PostMapping("/tg-chat/{id}")
    ResponseEntity<?> registerChat(@PathVariable Long id) {
        if (chatService.saveChat(id) != null) {
            log.atInfo()
                    .setMessage("Чат успешно зарегистрирован")
                    .addKeyValue("chat-id", id)
                    .log();
            return new ResponseEntity<>("Вы зарегистрированы", HttpStatus.OK);
        }

        log.atError()
                .setMessage("Чат не зарегистрирован")
                .addKeyValue("chat-id", id)
                .log();

        return new ResponseEntity<>(
                new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", List.of()),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Эндпоинт для удаления чата из БД
     *
     * @param id идентификатор чата
     * @return NOT_FOUND, если чат не найден в БД, OK, если чат успешно удален
     */
    @DeleteMapping("/tg-chat/{id}")
    ResponseEntity<?> deleteChat(@PathVariable Long id) {
        if (chatService.getPlainTgChatByChatId(id).isEmpty()) {
            log.atError()
                    .setMessage("Чат для удаления не найден")
                    .addKeyValue("chat-id", id)
                    .log();

            return new ResponseEntity<>(
                    new ApiErrorResponse("Чат не существует", "404", "", "", new ArrayList<>()), HttpStatus.NOT_FOUND);
        }
        chatService.deleteChatByChatId(id);
        log.atInfo().setMessage("Чат успешно удален").addKeyValue("chat-id", id).log();

        return new ResponseEntity<>("Чат успешно удален", HttpStatus.OK);
    }

    /**
     * Эндпоинт для настройки времени отправки уведомлений в чат
     *
     * @param tgChatId идентификатор чата
     * @param timeConfig конфигурация времени
     * @return BAD_REQUEST, если чат не найден или не удалось обновить настройки, иначе - OK
     */
    @PatchMapping("/timeconfig")
    ResponseEntity<?> updateTimeConfiguration(
            @RequestHeader("Tg-Chat-Id") Long tgChatId, @RequestHeader("Time-Config") String timeConfig) {
        Optional<TgChat> chat = chatService.getPlainTgChatByChatId(tgChatId);
        if (chat.isEmpty() || !chatService.updateTimeConfig(chat.orElseThrow(), timeConfig)) {
            return new ResponseEntity<>(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", "", "", new ArrayList<>()),
                    HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().build();
    }
}
