package backend.academy.handler;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.RestClient;

/** Обработчик пользовательских команд. Может взаимодействовать с внешними сервисами для получения ответа на команду */
public interface Handler {
    /**
     * Обработать команду и сформировать ответ
     *
     * @param update объект класса, содержащего информацию о команде пользователя
     * @param restClient клиент для взаимодействия с внешними сервисами через HTTP
     * @return {@code SendMessage} - ответ, посылаемый пользователю
     */
    SendMessage handle(Update update, RestClient restClient);

    /**
     * Проверить, можно ли обработать данную команду текущим обработчиком
     *
     * @param command команда для проверки
     * @return {@code true} - если команда поддерживается обработчиком, иначе - {@code false}
     */
    boolean supportCommand(Command command);

    default SendMessage onError(Update update, RestClient restClient, Throwable t) {
        return new SendMessage(update.message().chat().id(), "Ошибка. Не удалось выполнить запрос :(");
    }

    default SendMessage onCBError(Update update, RestClient restClient, CallNotPermittedException t) {
        return new SendMessage(update.message().chat().id(), "Ошибка. Сервис недоступен, попробуйте позже :(");
    }
}
