package backend.academy.processor.impl;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.bot.commands.Command;
import backend.academy.crawler.CrawlerManager;
import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.MessageCrawler;
import backend.academy.handler.Handler;
import backend.academy.handler.HandlerManager;
import backend.academy.processor.Processor;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TgChatProcessor implements Processor {
    private final RestClient restClient;
    private final HandlerManager handlerManager;
    private final CrawlerManager crawlerManager;

    @Autowired
    public TgChatProcessor(HandlerManager handlerManager, RestClient restClient, CrawlerManager crawlerManager) {
        this.handlerManager = handlerManager;
        this.restClient = restClient;
        this.crawlerManager = crawlerManager;
    }

    @Override
    public SendMessage process(Update update) {
        for (Map.Entry<MessageCrawler, Handler> pair : crawlerManager.getCrawlerHandlerPair()) {
            DialogStateDTO dialogStateDTO = pair.getKey().crawl(update);
            if (dialogStateDTO.message() != null) {
                return dialogStateDTO.message();
            } else if (dialogStateDTO.isCompleted()) {
                return pair.getValue().handle(update, restClient);
            }
        }

        Command command = BotCommandsStorage.getCommand(update.message().text());
        Handler messageHandler = handlerManager.manageHandler(command);

        return messageHandler.handle(update, restClient);
    }
}
