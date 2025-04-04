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
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
public class ListByTagMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();
        String tagValue =
                Arrays.stream(update.message().text().split(" ", 2)).toList().getLast();

        log.atInfo()
                .setMessage("Запрос списка отслеживаемых ресурсов по тегу")
                .addKeyValue("chat-id", chatId)
                .log();

        try {
            ListLinksResponse linksResponse = restClient
                    .get()
                    .uri("/linksbytag")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .header("Tag-Value", tagValue)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                            ApiErrorResponse apiErrorResponse =
                                    objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                            throw new ApiErrorException(apiErrorResponse);
                        }
                        return objectMapper.readValue(response.getBody(), ListLinksResponse.class);
                    });

            if (linksResponse == null) {
                return new SendMessage(chatId, "Ошибка при получении списка ресурсов");
            }

            String trackedLinks = getTrackedLinksAsString(linksResponse, tagValue);
            return new SendMessage(chatId, trackedLinks);
        } catch (ApiErrorException e) {
            log.atError()
                    .setMessage("Некорректные параметры запроса списка ресурсов")
                    .addKeyValue("error", e)
                    .addKeyValue("chat-id", chatId)
                    .log();

            return new SendMessage(chatId, e.getApiErrorResponse().description());
        }
    }

    private String getTrackedLinksAsString(ListLinksResponse linksResponse, String tagName) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        builder.append("Количество отслеживаемых ресурсов (для тега " + tagName + "): ")
                .append(linksResponse.size())
                .append('\n');
        for (Link link : linksResponse.links()) {
            builder.append(++counter).append(") ").append(link.getUrl()).append('\n');
        }
        return builder.toString();
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/listbytag") && command.needExtraInfo();
    }
}
