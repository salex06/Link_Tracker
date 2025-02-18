package backend.academy.api;

import backend.academy.bot.Bot;
import backend.academy.dto.LinkUpdate;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {
    private final Bot bot;

    @Autowired
    public BotController(Bot bot) {
        this.bot = bot;
    }

    @PostMapping("/updates")
    ResponseEntity<?> update(@RequestBody LinkUpdate linkUpdate) {
        List<Long> tgChatIds = linkUpdate.tgChatIds();
        String description = linkUpdate.description();
        String url = linkUpdate.url();
        Long id = linkUpdate.id();

        for (Long chatId : tgChatIds) {
            String responseText = String.format("Новое уведомление от ресурса %s (ID: %d): %s", url, id, description);
            SendMessage message = new SendMessage(chatId, responseText);
            bot.sendMessage(message);
        }
        return ResponseEntity.ok("");
    }
}
