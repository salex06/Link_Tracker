package backend.academy.handler;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.web.client.RestClient;

public interface Handler {
    SendMessage handle(Update update, RestClient restClient);

    boolean supportCommand(Command command);
}
