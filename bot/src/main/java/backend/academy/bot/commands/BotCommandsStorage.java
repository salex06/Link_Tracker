package backend.academy.bot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BotCommandsStorage {
    private BotCommandsStorage() {}

    private static final List<Command> COMMANDS = new ArrayList<>();

    private static final Map<String, String> COMMAND_DESCRIPTION = Map.of(
            "/start", "Запустить бота",
            "/help", "Вывести все команды на экран",
            "/track", "Запустить отслеживание ресурса по ссылке, следующей за командой",
            "/untrack", "Прекратить отслеживание ресурса по ссылке, следующей за командой",
            "/list", "Получить список всех отслеживаемых ресурсов",
            "/listbytag", "Получить список ссылок с данным тегом",
            "/addtag", "Добавить тег для ссылки");

    static {
        COMMANDS.add(new Command("/start", false));
        COMMANDS.add(new Command("/help", false));
        COMMANDS.add(new Command("/list", false));
        COMMANDS.add(new Command("/track", false));
        COMMANDS.add(new Command("/untrack", true));
        COMMANDS.add(new Command("/listbytag", true));
        COMMANDS.add(new Command("/addtag", true));
    }

    public static List<Command> getCommands() {
        return Collections.unmodifiableList(COMMANDS);
    }

    /**
     * Возвращает команду по её строковому значению
     *
     * @param message название команды
     * @return объект типа {@code Command}. Если сообщение не соответствует ни одной команде, возвращается {@code null}
     */
    public static Command getCommand(String message) {
        for (Command command : COMMANDS) {
            if (Command.isValidCommand(command, message)) {
                return command;
            }
        }

        return null;
    }

    /**
     * Возвращает таблицу пар "значение команды - описание команды"
     *
     * @return {@code Map<String, String>} - набор пар "значение комманды - описание команды"
     */
    public static Map<String, String> getCommandDescription() {
        return COMMAND_DESCRIPTION;
    }
}
