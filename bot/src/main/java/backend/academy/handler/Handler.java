package backend.academy.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface Handler {
    SendMessage handle(Update update);
}
