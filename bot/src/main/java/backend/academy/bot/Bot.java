package backend.academy.bot;

import backend.academy.processor.Processor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Component
public class Bot {
    private final SetMyCommands commands;
    private final TelegramBot bot;

    /**
     * Конструктор с параметрами
     *
     * @param botConfig конфигурация бота (токен и имя бота)
     * @param tgChatProcessor обработчик сообщений пользователя
     */
    public Bot(BotConfig botConfig, Processor tgChatProcessor, SetMyCommands setMyCommands) {
        this.bot = new TelegramBot(botConfig.telegramToken());

        this.bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                SendMessage message = tgChatProcessor.process(update);
                execute(message);
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        this.commands = setMyCommands;
        bot.execute(commands);
    }

    /**
     * Параметризованный конструктор с аргументом - экземпляром бота
     *
     * @param telegramBot экземпляр телеграм бота
     * @param tgChatProcessor обработчик сообщений пользователя
     */
    @Autowired
    public Bot(TelegramBot telegramBot, Processor tgChatProcessor, SetMyCommands setMyCommands) {
        this.bot = telegramBot;

        this.bot.setUpdatesListener(
                updates -> {
                    updates.forEach(update -> {
                        try {
                            if (update.message() == null) {
                                return;
                            }
                            SendMessage message = tgChatProcessor.process(update);
                            execute(message);
                        } catch (ResourceAccessException e) {
                            log.atError()
                                    .setMessage("Сервис scrapper недоступен")
                                    .log();
                        }
                    });
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> {
                    if (e.response() != null) {
                        log.atError()
                                .setMessage("Ошибка Telegram API")
                                .addKeyValue("error-code", e.response().errorCode())
                                .addKeyValue("description", e.response().description())
                                .log();
                    } else {
                        log.atError()
                                .setMessage("Проблемы с сетью")
                                .addKeyValue("stacktrace", e.getStackTrace())
                                .log();
                    }
                });

        this.commands = setMyCommands;
        bot.execute(commands);
    }

    /**
     * Параметризованный конструктор с одним параметром
     *
     * @param telegramBot экземпляр телеграм бота (должен быть установлен UpdatesListener)
     */
    public Bot(TelegramBot telegramBot) {
        this.bot = telegramBot;

        this.commands = null;
        bot.execute(commands);
    }

    public void execute(SendMessage message) {
        bot.execute(message);
    }
}
