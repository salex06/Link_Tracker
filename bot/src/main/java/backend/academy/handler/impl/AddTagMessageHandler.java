package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.crawler.MessageCrawler;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import backend.academy.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
@SuppressWarnings("CPD-START")
public class AddTagMessageHandler implements Handler {
    private final MessageCrawler crawler;
    private final RedisCacheService redisCacheService;
    private final RetryTemplate retryTemplate;

    @Autowired
    public AddTagMessageHandler(
            @Qualifier("addTagCrawler") MessageCrawler crawler,
            RedisCacheService redisCachedService,
            RetryTemplate retryTemplate) {
        this.crawler = crawler;
        this.redisCacheService = redisCachedService;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        redisCacheService.invalidateCache();

        ObjectMapper objectMapper = new ObjectMapper();

        Long chatId = update.message().chat().id();
        AddLinkRequest crawlerReport = crawler.terminate(chatId);
        if (crawlerReport == null
                || crawlerReport.tags() == null
                || crawlerReport.tags().size() != 1) {
            log.atError()
                    .setMessage("Отсутствуют данные о ресурсе для передачи")
                    .addKeyValue("chat-id", chatId)
                    .log();
            return new SendMessage(chatId, "Ошибка, попробуйте снова");
        }

        log.atInfo()
                .setMessage("Запрос на добавление тега для ссылки")
                .addKeyValue("chat-id", chatId)
                .addKeyValue("url", crawlerReport.link())
                .addKeyValue("tags", crawlerReport.tags())
                .log();

        try {
            LinkResponse linkResponse = retryTemplate.execute(
                    context -> restClient
                            .post()
                            .uri("/addtag")
                            .header("Add-To-All", Objects.equals(crawlerReport.link(), "ALL") ? "true" : "false")
                            .header("Tg-Chat-Id", String.valueOf(chatId))
                            .body(crawlerReport)
                            .exchange((request, response) -> {
                                if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                                    ApiErrorResponse apiErrorResponse =
                                            objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
                                    throw new ApiErrorException(apiErrorResponse);
                                } else if (response.getStatusCode().isError()) {
                                    throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                                } else {
                                    return objectMapper.readValue(response.getBody(), LinkResponse.class);
                                }
                            }),
                    context -> {
                        if (context.getLastThrowable() instanceof ApiErrorException e) {
                            throw e;
                        }
                        return null;
                    });

            if (linkResponse == null) {
                return new SendMessage(chatId, "Ошибка при ответе на запрос добавления тега");
            }

            return new SendMessage(chatId, "Тег успешно добавлен!");
        } catch (ApiErrorException e) {
            log.atError()
                    .setMessage("Некорректные параметры при запросе на добавление тега")
                    .addKeyValue("chat-id", chatId)
                    .addKeyValue("link", crawlerReport.link())
                    .addKeyValue("tags", crawlerReport.tags())
                    .log();

            return new SendMessage(chatId, e.getApiErrorResponse().description());
        }
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/addtag") && command.needExtraInfo();
    }
}
