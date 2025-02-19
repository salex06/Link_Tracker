package backend.academy.handler;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface Handler {
    SendMessage handle(Update update);

    boolean supportCommand(Command command);
}
