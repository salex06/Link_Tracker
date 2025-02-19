package backend.academy.bot.commands;

public record Command(String commandName, boolean needExtraInfo) {
    public static boolean isValidCommand(Command command, String message) {
        if (command.needExtraInfo) {
            String[] splittedMessage = message.split(" ", 2);
            return splittedMessage.length == 2 && splittedMessage[0].equals(command.commandName);
        }
        return message.equals(command.commandName);
    }
}
