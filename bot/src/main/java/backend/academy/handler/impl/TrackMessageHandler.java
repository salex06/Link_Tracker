package backend.academy.handler.impl;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class TrackMessageHandler implements Handler {
    @Override
    public SendMessage handle(Update update) {
        RestClient restClient = RestClient.create();
        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();
        String message = update.message().text();
        String[] splittedMessage = message.split(" ", 2);
        if (splittedMessage.length <= 1) {
            return new SendMessage(chatId, "Вы должны указать URL после команды!");
        }
        String linkUrlToTrack = splittedMessage[1];

        String url = "http://localhost:8081/links";

        try {
            ResponseEntity<LinkResponse> entity = restClient
                    .post()
                    .uri(url)
                    .body(new AddLinkRequest(linkUrlToTrack))
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        ApiErrorResponse apiErrorResponse =
                                objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                        throw new ApiErrorException(apiErrorResponse);
                    })
                    .toEntity(LinkResponse.class);
            LinkResponse linkResponse = entity.getBody();
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
}
