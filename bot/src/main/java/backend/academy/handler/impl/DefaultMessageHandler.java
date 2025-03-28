package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.handler.Handler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(3)
@Component
public class DefaultMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient rest) {
        Long chatId = update.message().chat().id();
        String message = update.message().text();

        log.atInfo()
                .setMessage("От пользователя получена неизвестная команда")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("user-message", message)
                .log();

        return new SendMessage(chatId, "Неизвестная команда: " + message);
    }

    @Override
    public boolean supportCommand(Command command) {
        return true;
    }
}
