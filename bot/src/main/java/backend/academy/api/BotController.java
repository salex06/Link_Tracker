package backend.academy.api;

import backend.academy.bot.Bot;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BotController {
    private final Bot bot;

    @Autowired
    public BotController(Bot bot) {
        this.bot = bot;
    }

    /**
     * Обработать команду уведомления пользователей об обновлении ресурсов
     *
     * @param linkUpdate DTO, хранящий информацию об обновлении ресурса
     * @return {@code ResponseEntity<?>} - ответ на команду (ApiErrorResponse с кодом 400 при ошибке, иначе - OK с
     *     пустым телом ответа)
     */
    @PostMapping("/updates")
    public ResponseEntity<?> update(@RequestBody LinkUpdate linkUpdate) {
        List<Long> tgChatIds = linkUpdate.tgChatIds();
        String description = linkUpdate.description();
        String url = linkUpdate.url();
        Long id = linkUpdate.id();

        log.atInfo()
                .setMessage("Новый запрос к эндпоинту /updates")
                .addKeyValue("url", url)
                .addKeyValue("description", description)
                .addKeyValue("tg-chat-ids", tgChatIds)
                .log();

        if (LinkUpdate.anyFieldIsNull(linkUpdate)) {
            throw new ApiErrorException(
                    new ApiErrorResponse("Некорректные параметры запроса", "400", null, null, null));
        }

        sendOutMessages(tgChatIds, url, id, description);

        return ResponseEntity.ok("");
    }

    private void sendOutMessages(List<Long> tgChatIds, String url, Long id, String description) {
        for (Long chatId : tgChatIds) {
            String responseText = String.format("Новое уведомление от ресурса %s (ID: %d): %s", url, id, description);
            SendMessage message = new SendMessage(chatId, responseText);
            bot.execute(message);
        }
    }
}
