package backend.academy.processor;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

/** Интерфейс предоставляет метод process для обработки сообщений пользователя и формирования ответа */
public interface Processor {
    /**
     * Обработать команду пользователя
     *
     * @param update информация о сообщении от пользователя
     * @return {@code SendMessage} - ответ бота на команду
     */
    SendMessage process(Update update);
}
