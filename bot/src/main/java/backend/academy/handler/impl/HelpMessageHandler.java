package backend.academy.handler.impl;

import backend.academy.bot.commands.Command;
import backend.academy.handler.Handler;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class HelpMessageHandler implements Handler {
    private final Map<String, String> helpCommands = Map.of(
            "/start", "Запустить бота",
            "/help", "Вывести все команды на экран",
            "/track", "Запустить отслеживание ресурса по ссылке, следующей за командой",
            "/untrack", "Прекратить отслеживание ресурса по ссылке, следующей за командой",
            "/list", "Получить список всех отслеживаемых ресурсов");

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String commands = getCommandsAsString();
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
