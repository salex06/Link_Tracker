package backend.academy.processor.impl;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.bot.commands.Command;
import backend.academy.handler.Handler;
import backend.academy.handler.HandlerManager;
import backend.academy.processor.Processor;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Обработчик сообщений от пользователя в тг чате */
@Component
public class TgChatProcessor implements Processor {
    private final HandlerManager handlerManager;

    @Autowired
    public TgChatProcessor(HandlerManager handlerManager) {
        this.handlerManager = handlerManager;
    }

    @Override
    public SendMessage process(Update update) {
        String message = update.message().text();

        Command command = BotCommandsStorage.getCommand(message);
        Handler messageHandler = handlerManager.manageHandler(command);

        return messageHandler.handle(update);
    }
}
