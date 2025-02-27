package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.handler.Handler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Обработчик запроса помощи. Предоставляет список доступных команд */
@Slf4j
@Order(2)
@Component
public class HelpMessageHandler implements Handler {
    private final Map<String, String> helpCommands;

    @Autowired
    public HelpMessageHandler(Map<String, String> helpCommands) {
        this.helpCommands = helpCommands;
    }

    @Override
    public SendMessage handle(Update update, RestClient restClient) {
        Long chatId = update.message().chat().id();
        String commands = getCommandsAsString();

        log.atInfo()
                .setMessage("Команда вывода справки")
                .addKeyValue("chat-id", chatId)
                .log();

        return new SendMessage(chatId, commands);
    }

    private String getCommandsAsString() {
        StringBuilder response = new StringBuilder();

        for (Map.Entry<String, String> helpCommand : helpCommands.entrySet()) {
            response.append(helpCommand.getKey())
                    .append(" - ")
                    .append(helpCommand.getValue())
                    .append('\n');
        }

        return response.toString();
    }

    @Override
    public boolean supportCommand(Command command) {
        return command != null && Objects.equals(command.commandName(), "/help") && !command.needExtraInfo();
    }
}
