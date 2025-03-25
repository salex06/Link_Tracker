package backend.academy.processor.impl;

import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.COMPLETED;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.bot.commands.Command;
import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.MessageCrawler;
import backend.academy.handler.Handler;
import backend.academy.handler.HandlerManager;
import backend.academy.handler.impl.TrackMessageHandler;
import backend.academy.processor.Processor;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TgChatProcessor implements Processor {
    private final RestClient restClient;
    private final HandlerManager handlerManager;
    private final MessageCrawler messageCrawler;
    private final Handler trackMessageHandler;

    @Autowired
    public TgChatProcessor(
            HandlerManager handlerManager,
            RestClient restClient,
            MessageCrawler messageCrawler,
            TrackMessageHandler trackMessageHandler) {
        this.handlerManager = handlerManager;
        this.restClient = restClient;
        this.messageCrawler = messageCrawler;
        this.trackMessageHandler = trackMessageHandler;
    }

    @Override
    public SendMessage process(Update update) {
        DialogStateDTO dialogStateDTO = messageCrawler.crawl(update);
        if (dialogStateDTO.message() != null) {
            return dialogStateDTO.message();
        }

        Handler messageHandler = trackMessageHandler;
        if (dialogStateDTO.state() != COMPLETED) {
            Command command = BotCommandsStorage.getCommand(update.message().text());
            messageHandler = handlerManager.manageHandler(command);
        }

        return messageHandler.handle(update, restClient);
    }
}
