package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
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
}
