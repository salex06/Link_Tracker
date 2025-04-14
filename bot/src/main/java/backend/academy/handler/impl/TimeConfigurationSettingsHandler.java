package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
public class TimeConfigurationSettingsHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        Message message = update.message();
        Long chatId = message.chat().id();
        String messageBody = message.text();
        String[] splittedMessage = messageBody.split(" ", 2);

        log.atInfo()
                .setMessage("Запрос на изменение конфигурации времени отправки уведомлений")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("message", messageBody)
                .log();

        if (splittedMessage.length < 2 || !TimeConfiguration.isValidConfiguration(splittedMessage[1])) {
            log.atWarn()
                    .setMessage("Некорректные данные конфигурации времени")
                    .addKeyValue("chat-id", chatId)
                    .addKeyValue("message", messageBody)
                    .log();
            return new SendMessage(chatId, "Ошибка! Попробуйте снова");
        }

        String timeConfig = splittedMessage[1];
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String linkResponse = restClient
                    .patch()
                    .uri("/timeconfig")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .header("Time-Config", String.valueOf(timeConfig))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                            ApiErrorResponse apiErrorResponse =
                                    objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                            throw new ApiErrorException(apiErrorResponse);
                        }
                        return "";
                    });

            if (linkResponse == null) {
                return new SendMessage(chatId, "Ошибка при ответе на запрос настройки конфигурации времени");
            }

            return new SendMessage(chatId, "Конфигурация времени отправки уведомлений прошла успешно!");
        } catch (ApiErrorException e) {
            log.atError()
                    .setMessage("Некорректные параметры при запросе настройки конфигурации времени")
                    .addKeyValue("chat-id", chatId)
                    .addKeyValue("message", messageBody)
                    .addKeyValue("exception", e.getApiErrorResponse().description())
                    .log();

            return new SendMessage(chatId, e.getApiErrorResponse().description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/timeconfig") && command.needExtraInfo();
    }

    public enum TimeConfiguration {
        IMMEDIATELY("immediately"),
        SPECIFIC_TIME("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

        TimeConfiguration(String pattern) {
            configDescription = pattern;
        }

        private String configDescription;

        public static boolean isValidConfiguration(String configuration) {
            for (TimeConfiguration current : TimeConfiguration.values()) {
                if (configuration.matches(current.configDescription)) {
                    return true;
                }
            }
            return false;
        }
    }
}
