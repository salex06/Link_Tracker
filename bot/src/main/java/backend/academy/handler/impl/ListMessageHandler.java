package backend.academy.handler.impl;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import backend.academy.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class ListMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update) {
        RestClient restClient = RestClient.create();
        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();

        String url = "http://localhost:8081/links";

        try {
            ResponseEntity<ListLinksResponse> entity = restClient
                    .get()
                    .uri(url)
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        ApiErrorResponse apiErrorResponse =
                                objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                        throw new ApiErrorException(apiErrorResponse);
                    })
                    .toEntity(ListLinksResponse.class);
            ListLinksResponse linksResponse = entity.getBody();
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
                .append(linksResponse.getSize())
                .append('\n');
        for (Link link : linksResponse.getLinks()) {
            builder.append(++counter).append(") ").append(link.getUrl()).append('\n');
        }
        return builder.toString();
    }
}
