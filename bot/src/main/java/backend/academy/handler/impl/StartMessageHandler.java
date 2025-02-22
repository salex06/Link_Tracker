package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Objects;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Order(2)
@Component
public class StartMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        Long chatId = update.message().chat().id();
        ObjectMapper objectMapper = new ObjectMapper();

        String url = "http://localhost:8081/tg-chat/" + chatId.toString();

        try {
            ResponseEntity<String> data = restClient
                    .post()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        ApiErrorResponse apiErrorResponse =
                                objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                        throw new ApiErrorException(apiErrorResponse);
                    })
                    .toEntity(String.class);
            return new SendMessage(chatId, data.getBody());
        } catch (ApiErrorException e) {
            ApiErrorResponse apiErrorResponse = e.apiErrorResponse();
            // TODO: добавить логирование
            return new SendMessage(chatId, apiErrorResponse.description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/start") && !command.needExtraInfo();
    }
}
