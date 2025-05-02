package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinksResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.handler.Handler;
import backend.academy.model.Link;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings("CPD-START")
public class ListMessageHandler implements Handler {
    public static final String LIST_CACHE_PREFIX = "list:";

    private final RedisCacheService redisCacheService;
    private final ApplicationStabilityProperties stabilityProperties;

    @Override
    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    public SendMessage handle(Update update, RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();

        log.atInfo()
                .setMessage("Запрос списка отслеживаемых ресурсов")
                .addKeyValue("chat-id", chatId)
                .log();

        String cachedResponse = redisCacheService.getValue(LIST_CACHE_PREFIX + chatId);
        if (cachedResponse != null) {
            log.atDebug()
                    .setMessage("Возвращен кешированный результат")
                    .addKeyValue("command", "/list")
                    .addKeyValue("chat-id", chatId)
                    .log();

            return new SendMessage(chatId, cachedResponse);
        }

        try {
            List<Integer> retryableHttpCodes = stabilityProperties.getRetry().getHttpCodes();
            ListLinksResponse linksResponse = restClient
                    .get()
                    .uri("/links")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                            ApiErrorResponse apiErrorResponse =
                                    objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                            throw new ApiErrorException(apiErrorResponse);
                        } else if (retryableHttpCodes.contains(
                                response.getStatusCode().value())) {
                            throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                        } else if (response.getStatusCode().isError()) {
                            throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                        }
                        return objectMapper.readValue(response.getBody(), ListLinksResponse.class);
                    });

            if (linksResponse == null) {
                return new SendMessage(chatId, "Ошибка при получении списка ресурсов");
            }

            String trackedLinks = getTrackedLinksAsString(linksResponse);

            redisCacheService.putValue(LIST_CACHE_PREFIX + chatId, trackedLinks);

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

    private String getTrackedLinksAsString(ListLinksResponse linksResponse) {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        builder.append("Количество отслеживаемых ресурсов: ")
                .append(linksResponse.size())
                .append('\n');
        for (Link link : linksResponse.links()) {
            builder.append(++counter).append(") ").append(link.getUrl()).append('\n');
            builder.append("Теги: ");
            for (String tag : link.getTags()) {
                builder.append(tag).append('\t');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/list") && !command.needExtraInfo();
    }
}
