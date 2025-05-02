package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.handler.Handler;
import backend.academy.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings("CPD-START")
public class UntrackMessageHandler implements Handler {
    private final RedisCacheService redisCacheService;
    private final ApplicationStabilityProperties stabilityProperties;

    @Override
    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    public SendMessage handle(Update update, RestClient restClient) {
        redisCacheService.invalidateCache();

        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();
        String message = update.message().text();
        String[] splittedMessage = message.split(" ", 2);
        if (splittedMessage.length <= 1) {
            log.atError()
                    .setMessage("Некорректная команда")
                    .addKeyValue("command", message)
                    .addKeyValue("chat-id", chatId)
                    .log();

            return new SendMessage(chatId, "Вы должны указать URL после команды!");
        }

        String linkUrlToUntrack = splittedMessage[1];

        log.atInfo()
                .setMessage("Запрос на прекращение отслеживания ресурса")
                .addKeyValue("url", linkUrlToUntrack)
                .addKeyValue("chat-id", chatId)
                .log();

        try {
            List<Integer> retryableHttpCodes = stabilityProperties.getRetry().getHttpCodes();
            LinkResponse linkResponse = restClient
                    .method(HttpMethod.DELETE)
                    .uri("/links")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .body(new RemoveLinkRequest(linkUrlToUntrack))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)
                                || response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                            ApiErrorResponse apiErrorResponse =
                                    objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                            throw new ApiErrorException(apiErrorResponse);
                        } else if (retryableHttpCodes.contains(
                                response.getStatusCode().value())) {
                            throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                        } else if (response.getStatusCode().isError()) {
                            throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                        }
                        return objectMapper.readValue(response.getBody(), LinkResponse.class);
                    });

            if (linkResponse == null) {
                return new SendMessage(chatId, "Ошибка при ответе на запрос отслеживания");
            }

            return new SendMessage(
                    chatId,
                    String.format("Ресурс %s удален из отслеживаемых. ID: %d", linkResponse.url(), linkResponse.id()));
        } catch (ApiErrorException e) {
            log.atError()
                    .setMessage("Некорректные параметры при запросе на прекращение отслеживания ссылки")
                    .addKeyValue("chat-id", chatId)
                    .log();

            return new SendMessage(chatId, e.getApiErrorResponse().description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/untrack") && command.needExtraInfo();
    }
}
