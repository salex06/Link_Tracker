package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Objects;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Обработчик команды добавления ресурса на отслеживание */
@Order(2)
@Component
public class TrackMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();
        String message = update.message().text();
        String[] splittedMessage = message.split(" ", 2);
        if (splittedMessage.length <= 1) {
            return new SendMessage(chatId, "Вы должны указать URL после команды!");
        }
        String linkUrlToTrack = splittedMessage[1];

        try {
            LinkResponse linkResponse = restClient
                    .post()
                    .uri("/links")
                    .body(new AddLinkRequest(linkUrlToTrack))
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                            ApiErrorResponse apiErrorResponse =
                                    objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                            throw new ApiErrorException(apiErrorResponse);
                        } else {
                            return objectMapper.readValue(response.getBody(), LinkResponse.class);
                        }
                    });
            if (linkResponse == null) {
                return new SendMessage(chatId, "Ошибка при ответе на запрос отслеживания");
            }
            return new SendMessage(
                    chatId,
                    String.format(
                            "Ресурс %s добавлен для отслеживания. ID: %d", linkResponse.url(), linkResponse.id()));
        } catch (ApiErrorException e) {
            // TODO: добавить логирование
            return new SendMessage(chatId, e.apiErrorResponse().description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/track") && command.needExtraInfo();
    }
}
