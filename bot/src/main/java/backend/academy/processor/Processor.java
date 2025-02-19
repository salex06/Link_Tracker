package backend.academy.processor;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface Processor {
    SendMessage process(Update update);
}
