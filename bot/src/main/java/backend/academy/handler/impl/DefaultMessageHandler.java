package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.handler.Handler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class DefaultMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String message = update.message().text();

        return new SendMessage(chatId, "Неизвестная команда: " + message);
    }

    @Override
    public boolean supportCommand(Command command) {
        return true;
    }
}
