package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.crawler.MessageCrawler;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@SuppressWarnings("CPD-START")
public class RemoveTagMessageHandler implements Handler {
    private final MessageCrawler crawler;
    private final RedisCacheService redisCacheService;
    private final ApplicationStabilityProperties stabilityProperties;

    @Autowired
    public RemoveTagMessageHandler(
            @Qualifier("eraseTagCrawler") MessageCrawler crawler,
            RedisCacheService redisCacheService,
            ApplicationStabilityProperties stabilityProperties) {
        this.crawler = crawler;
        this.redisCacheService = redisCacheService;

        this.stabilityProperties = stabilityProperties;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    public SendMessage handle(Update update, RestClient restClient) {
        redisCacheService.invalidateCache();

        Long chatId = update.message().chat().id();
        AddLinkRequest crawlerReport = crawler.terminate(chatId);
        if (crawlerReport == null
                || crawlerReport.tags() == null
                || crawlerReport.tags().size() != 1) {
            log.atError()
                    .setMessage("Отсутствуют данные о ресурсе для удаления тега")
                    .addKeyValue("chat-id", chatId)
                    .log();
            return new SendMessage(chatId, "Ошибка, попробуйте снова");
        }

        log.atInfo()
                .setMessage("Запрос на удаление тега для ссылки")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("url", crawlerReport.link())
                .addKeyValue("tags", crawlerReport.tags())
                .log();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Integer> retryableHttpCodes = stabilityProperties.getRetry().getHttpCodes();
            LinkResponse linkResponse = restClient
                    .method(HttpMethod.DELETE)
                    .uri("/removetag")
                    .header("Tg-Chat-Id", String.valueOf(chatId))
                    .header("Remove-To-All", Objects.equals(crawlerReport.link(), "ALL") ? "true" : "false")
                    .body(crawlerReport)
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
                        return objectMapper.readValue(response.getBody(), LinkResponse.class);
                    });

            if (linkResponse == null) {
                return new SendMessage(chatId, "Ошибка при ответе на запрос удаления тега");
            }

            return new SendMessage(chatId, "Тег успешно удален!");
        } catch (ApiErrorException e) {
            log.atError()
                    .setMessage("Некорректные параметры при запросе на удаление тега")
                    .addKeyValue("chat-id", chatId)
                    .addKeyValue("link", crawlerReport.link())
                    .addKeyValue("tags", crawlerReport.tags())
                    .log();

            return new SendMessage(chatId, e.getApiErrorResponse().description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/removetag") && command.needExtraInfo();
    }
}
