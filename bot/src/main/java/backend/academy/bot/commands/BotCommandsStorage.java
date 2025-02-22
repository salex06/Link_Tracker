package backend.academy.bot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Хранилище доступных команд бота */
public class BotCommandsStorage {
    private BotCommandsStorage() {}

    private static final List<Command> COMMANDS = new ArrayList<>();

    private static final Map<String, String> COMMAND_DESCRIPTION = Map.of(
            "/start", "Запустить бота",
            "/help", "Вывести все команды на экран",
            "/track", "Запустить отслеживание ресурса по ссылке, следующей за командой",
            "/untrack", "Прекратить отслеживание ресурса по ссылке, следующей за командой",
            "/list", "Получить список всех отслеживаемых ресурсов");

    static {
        COMMANDS.add(new Command("/start", false));
        COMMANDS.add(new Command("/help", false));
        COMMANDS.add(new Command("/list", false));
        COMMANDS.add(new Command("/track", true));
        COMMANDS.add(new Command("/untrack", true));
    }

    /**
     * Возвращает список доступных команд
     *
     * @return {@code List<Command>} - список доступных команд
     */
    public static List<Command> getCommands() {
        return Collections.unmodifiableList(COMMANDS);
    }

    /**
     * Возвращает команду по её строковому значению
     *
     * @param message название команды
     * @return объект типа {@code Command}
     */
    public static Command getCommand(String message) {
        for (Command command : COMMANDS) {
            if (Command.isValidCommand(command, message)) {
                return command;
            }
        }
        return null;
    }

    public static Map<String, String> getCommandDescription() {
        return COMMAND_DESCRIPTION;
    }
}
