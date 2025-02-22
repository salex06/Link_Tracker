package backend.academy.handler;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.handler.impl.HelpMessageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Конфигурационный класс для настройки классов-обработчиков сообщений */
@Configuration
public class HandlerBeans {
    /**
     * Возвращает сконструированный объект класса RestClient
     *
     * @param baseUrl базовый Url
     * @return объект класса RestClient
     */
    @Bean
    public RestClient restClient(@Value("${scrapper.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public HelpMessageHandler helpMessageHandler() {
        return new HelpMessageHandler(BotCommandsStorage.getCommandDescription());
    }
}
