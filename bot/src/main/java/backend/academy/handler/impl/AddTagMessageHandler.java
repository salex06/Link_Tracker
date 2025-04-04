package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.crawler.MessageCrawler;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkResponse;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.handler.Handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Order(2)
@Component
public class AddTagMessageHandler implements Handler {
    private final MessageCrawler crawler;

    @Autowired
    public AddTagMessageHandler(@Qualifier("addTagCrawler") MessageCrawler crawler) {
        this.crawler = crawler;
    }

    @Override
    public SendMessage handle(Update update, RestClient restClient) {
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
            LinkResponse linkResponse = restClient
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
                        } else {
                            return objectMapper.readValue(response.getBody(), LinkResponse.class);
                        }
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
