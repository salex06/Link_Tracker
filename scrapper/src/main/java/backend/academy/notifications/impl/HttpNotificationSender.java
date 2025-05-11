package backend.academy.notifications.impl;

import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.notifications.NotificationSender;
import backend.academy.notifications.fallback.FallbackSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "HTTP")
public class HttpNotificationSender implements NotificationSender {
    private final RestClient botUpdatesClient;
    private final ApplicationStabilityProperties stabilityProperties;
    private final FallbackSender fallbackSender;

    public HttpNotificationSender(
            @Qualifier("botConnectionClient") RestClient client,
            ApplicationStabilityProperties stabilityProperties,
            FallbackSender fallbackSender) {
        this.botUpdatesClient = client;
        this.stabilityProperties = stabilityProperties;
        this.fallbackSender = fallbackSender;
    }

    @Retry(name = "default", fallbackMethod = "onSendError")
    @CircuitBreaker(name = "default", fallbackMethod = "onSendCBError")
    public String send(LinkUpdate update) {
        try {
            log.atInfo()
                    .setMessage("Отправка уведомления об обновлении")
                    .addKeyValue("url", update.url())
                    .addKeyValue("description", update.description())
                    .addKeyValue("tg-chat-ids", update.tgChatIds())
                    .log();

            return botUpdatesClient.post().uri("/updates").body(update).exchange((request, response) -> {
                if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                    ApiErrorResponse apiErrorResponse =
                            new ObjectMapper().readValue(response.getBody(), ApiErrorResponse.class);
                    throw new ApiErrorException(apiErrorResponse);
                } else if (stabilityProperties
                        .getRetry()
                        .getHttpCodes()
                        .contains(response.getStatusCode().value())) {
                    throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                } else if (response.getStatusCode().isError()) {
                    throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                }
                return "OK";
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
            return "Api error";
        }
    }

    public String onSendError(LinkUpdate update, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при отправке уведомлений. Неудачный запрос")
                .addKeyValue("id", update.id())
                .addKeyValue("url", update.url())
                .addKeyValue("description", update.description())
                .addKeyValue("tg-chat-ids", update.tgChatIds())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return "Retry fallback";
    }

    public String onSendCBError(LinkUpdate update, CallNotPermittedException t) {
        log.atWarn()
                .setMessage("Ошибка при отправке уведомлений. Сервис недоступен. Переключение на резервный sender...")
                .addKeyValue("id", update.id())
                .addKeyValue("url", update.url())
                .addKeyValue("description", update.description())
                .addKeyValue("tg-chat-ids", update.tgChatIds())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        fallbackSender.send(update);
        return "CircuitBreaker fallback";
    }
}
