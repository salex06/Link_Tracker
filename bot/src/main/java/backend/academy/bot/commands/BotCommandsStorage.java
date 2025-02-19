package backend.academy.bot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotCommandsStorage {
    private BotCommandsStorage() {}

    private static final List<Command> COMMANDS = new ArrayList<>();

    static {
        COMMANDS.add(new Command("/start", false));
        COMMANDS.add(new Command("/help", false));
        COMMANDS.add(new Command("/list", false));
        COMMANDS.add(new Command("/track", true));
        COMMANDS.add(new Command("/untrack", true));
    }

    public static List<Command> getCommands() {
        return Collections.unmodifiableList(COMMANDS);
    }

    public static void setCommand(Command command) {
        COMMANDS.add(command);
    }

    public static Command getCommand(String message) {
        for (Command command : COMMANDS) {
            if (Command.isValidCommand(command, message)) {
                return command;
            }
        }
        return null;
    }
}
