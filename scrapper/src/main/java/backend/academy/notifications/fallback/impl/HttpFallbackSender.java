package backend.academy.notifications.fallback.impl;

import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.notifications.fallback.FallbackSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@ConditionalOnProperty(prefix = "app", name = "message-transport-fallback", havingValue = "HTTP")
@SuppressWarnings("CPD-START")
public class HttpFallbackSender implements FallbackSender {
    private final RestClient botUpdatesClient;
    private final ApplicationStabilityProperties stabilityProperties;

    public HttpFallbackSender(
            @Qualifier("botConnectionClient") RestClient client, ApplicationStabilityProperties stabilityProperties) {
        this.botUpdatesClient = client;
        this.stabilityProperties = stabilityProperties;
    }

    @Retry(name = "default", fallbackMethod = "onSendError")
    @CircuitBreaker(name = "default", fallbackMethod = "onSendCBError")
    public void send(LinkUpdate update) {
        try {
            log.atInfo()
                    .setMessage("Отправка уведомления об обновлении после fallback на Http")
                    .addKeyValue("url", update.url())
                    .addKeyValue("description", update.description())
                    .addKeyValue("tg-chat-ids", update.tgChatIds())
                    .log();

            botUpdatesClient.post().uri("/updates").body(update).exchange((request, response) -> {
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
                return "";
            });

        } catch (ApiErrorException e) {
            ApiErrorResponse response = e.getApiErrorResponse();
            log.atError()
                    .setMessage("Некорректные параметры запроса через HttpFallbackSender")
                    .addKeyValue("description", response.description())
                    .addKeyValue("code", response.code())
                    .addKeyValue("exception-name", response.exceptionName())
                    .addKeyValue("exception-message", response.exceptionName())
                    .log();
        }
    }

    public void onSendError(LinkUpdate update, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при отправке уведомлений через HttpFallbackSender. Неудачный запрос")
                .addKeyValue("id", update.id())
                .addKeyValue("url", update.url())
                .addKeyValue("description", update.description())
                .addKeyValue("tg-chat-ids", update.tgChatIds())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
    }

    public void onSendCBError(LinkUpdate update, CallNotPermittedException t) {
        log.atWarn()
                .setMessage("Ошибка при отправке уведомлений через HttpFallbackSender. Сервис недоступен")
                .addKeyValue("id", update.id())
                .addKeyValue("url", update.url())
                .addKeyValue("description", update.description())
                .addKeyValue("tg-chat-ids", update.tgChatIds())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
    }
}
