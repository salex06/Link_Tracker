package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings("CPD-START")
public class StartMessageHandler implements Handler {
    private final ApplicationStabilityProperties stabilityProperties;

    @Override
    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    public SendMessage handle(Update update, RestClient restClient) {
        Long chatId = update.message().chat().id();
        ObjectMapper objectMapper = new ObjectMapper();

        String url = "/tg-chat/" + chatId.toString();

        log.atInfo()
                .setMessage("Запрос на старт диалога")
                .addKeyValue("chat-id", chatId)
                .log();

        try {
            List<Integer> retryableHttpCodes = stabilityProperties.getRetry().getHttpCodes();
            String data = restClient.post().uri(url).exchange((request, response) -> {
                if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                    ApiErrorResponse apiErrorResponse =
                            objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                    throw new ApiErrorException(apiErrorResponse);
                } else if (retryableHttpCodes.contains(response.getStatusCode().value())) {
                    throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                } else if (response.getStatusCode().isError()) {
                    throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                }
                return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            });

            if (data == null) {
                return new SendMessage(chatId, "Ошибка при регистрации");
            }

            return new SendMessage(chatId, data);
        } catch (ApiErrorException e) {
            ApiErrorResponse apiErrorResponse = e.getApiErrorResponse();

            log.atError()
                    .setMessage("Некорректные параметры запроса")
                    .addKeyValue("error", e)
                    .addKeyValue("error-description", apiErrorResponse.description())
                    .addKeyValue("url", url)
                    .addKeyValue("chat-id", chatId)
                    .log();

            return new SendMessage(chatId, apiErrorResponse.description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/start") && !command.needExtraInfo();
    }
}
