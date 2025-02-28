package backend.academy.bot;

import backend.academy.bot.commands.BotCommandsStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Конфигурационный класс для TelegramBot */
@Configuration
public class BotBeans {
    /**
     * Создание bean-компонента - объекта класса TelegramBot
     *
     * @param botConfig настройки бота (токен и имя)
     * @return {@code TelegramBot} - объект класса TelegramBot
     */
    @Bean
    TelegramBot telegramBot(BotConfig botConfig) {
        return new TelegramBot(botConfig.telegramToken());
    }

    @Bean
    SetMyCommands setMyCommands() {
        List<BotCommand> botCommands = new ArrayList<>();
        BotCommandsStorage.getCommandDescription()
                .forEach((commandName, commandDescription) ->
                        botCommands.add(new BotCommand(commandName, commandDescription)));
        return new SetMyCommands(botCommands.toArray(new BotCommand[0]));
    }
}
