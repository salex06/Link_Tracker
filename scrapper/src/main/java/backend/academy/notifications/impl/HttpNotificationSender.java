package backend.academy.notifications.impl;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.notifications.NotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@Slf4j
public class HttpNotificationSender implements NotificationSender {
    private final RestClient botUpdatesClient;

    public HttpNotificationSender(RestClient client) {
        this.botUpdatesClient = client;
    }

    public void send(LinkUpdate update) {
        try {
            log.atInfo()
                    .setMessage("Отправка уведомления об обновлении")
                    .addKeyValue("url", update.url())
                    .addKeyValue("description", update.description())
                    .addKeyValue("tg-chat-ids", update.tgChatIds())
                    .log();
            botUpdatesClient.post().uri("/updates").body(update).exchange((request, response) -> {
                if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                    ApiErrorResponse apiErrorResponse =
                            new ObjectMapper().readValue(response.getBody(), ApiErrorResponse.class);
                    throw new ApiErrorException(apiErrorResponse);
                }
                return null;
            });
        } catch (ApiErrorException e) {
            ApiErrorResponse response = e.apiErrorResponse();
            log.atError()
                    .setMessage("Некорректные параметры запроса")
                    .addKeyValue("description", response.description())
                    .addKeyValue("code", response.code())
                    .addKeyValue("exception-name", response.exceptionName())
                    .addKeyValue("exception-message", response.exceptionName())
                    .log();
        }
    }
}
