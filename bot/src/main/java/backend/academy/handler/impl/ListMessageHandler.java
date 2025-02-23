package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import backend.academy.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Objects;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Обработчик команды получения списка отслеживаемых ресурсов */
@Order(2)
@Component
public class ListMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();

        try {
            ListLinksResponse linksResponse = restClient
                    .get()
                    .uri("/links")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                            ApiErrorResponse apiErrorResponse =
                                    objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                            throw new ApiErrorException(apiErrorResponse);
                        } else {
                            return objectMapper.readValue(response.getBody(), ListLinksResponse.class);
                        }
                    });
            if (linksResponse == null) {
                return new SendMessage(chatId, "Ошибка при получении списка ресурсов");
            }
            String trackedLinks = getTrackedLinksAsString(linksResponse);
            return new SendMessage(chatId, trackedLinks);
        } catch (ApiErrorException e) {
            // TODO: добавить логирование
            return new SendMessage(chatId, e.apiErrorResponse().description());
        }
    }

    private String getTrackedLinksAsString(ListLinksResponse linksResponse) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        builder.append("Количество отслеживаемых ресурсов: ")
                .append(linksResponse.size())
                .append('\n');
        for (Link link : linksResponse.links()) {
            builder.append(++counter).append(") ").append(link.getUrl()).append('\n');
        }
        return builder.toString();
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/list") && !command.needExtraInfo();
    }
}
