package backend.academy.notifications.impl;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.notifications.NotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "HTTP")
public class HttpNotificationSender implements NotificationSender {
    private final RestClient botUpdatesClient;
    private final RetryTemplate retryTemplate;

    public HttpNotificationSender(@Qualifier("botConnectionClient") RestClient client, RetryTemplate retryTemplate) {
        this.botUpdatesClient = client;
        this.retryTemplate = retryTemplate;
    }

    public void send(LinkUpdate update) {
        try {
            log.atInfo()
                    .setMessage("Отправка уведомления об обновлении")
                    .addKeyValue("url", update.url())
                    .addKeyValue("description", update.description())
                    .addKeyValue("tg-chat-ids", update.tgChatIds())
                    .log();

            retryTemplate.execute(
                    context -> botUpdatesClient
                            .post()
                            .uri("/updates")
                            .body(update)
                            .exchange((request, response) -> {
                                if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                                    ApiErrorResponse apiErrorResponse =
                                            new ObjectMapper().readValue(response.getBody(), ApiErrorResponse.class);
                                    throw new ApiErrorException(apiErrorResponse);
                                } else if (response.getStatusCode().isError()) {
                                    throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                                }
                                return null;
                            }),
                    context -> {
                        Throwable exception = context.getLastThrowable();
                        if (exception != null)
                            log.atError()
                                    .setMessage("Ошибка сервера при отправке уведомления")
                                    .addKeyValue("exception-message", exception.getMessage())
                                    .addKeyValue("stacktrace", exception.getStackTrace())
                                    .log();

                        if (exception instanceof ApiErrorException e) {
                            throw e;
                        }

                        return null;
                    });

        } catch (ApiErrorException e) {
            ApiErrorResponse response = e.getApiErrorResponse();
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
