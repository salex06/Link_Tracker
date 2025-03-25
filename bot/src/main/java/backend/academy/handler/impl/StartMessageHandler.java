package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
public class StartMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        Long chatId = update.message().chat().id();
        ObjectMapper objectMapper = new ObjectMapper();

        String url = "/tg-chat/" + chatId.toString();

        log.atInfo()
                .setMessage("Запрос на старт диалога")
                .addKeyValue("chat-id", chatId)
                .log();

        try {
            String data = restClient.post().uri(url).exchange((request, response) -> {
                if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                    ApiErrorResponse apiErrorResponse =
                            objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                    throw new ApiErrorException(apiErrorResponse);
                }
                return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            });

            return new SendMessage(chatId, data);
        } catch (ApiErrorException e) {
            ApiErrorResponse apiErrorResponse = e.apiErrorResponse();

            log.atError()
                    .setMessage("Некорректные параметры запроса")
                    .addKeyValue("error", e)
                    .addKeyValue("error-description", apiErrorResponse.description())
                    .addKeyValue("url", url)
                    .addKeyValue("chat-id", chatId)
                    .log();

            return new SendMessage(chatId, apiErrorResponse.description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/start") && !command.needExtraInfo();
    }
}
