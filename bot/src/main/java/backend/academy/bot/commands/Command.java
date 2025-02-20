package backend.academy.bot.commands;

/**
 * Сущность сообщения пользователя - команды бота
 *
 * @param commandName название команды
 * @param needExtraInfo индикатор необходимости дополнительных данных в сообщении с командой
 */
public record Command(String commandName, boolean needExtraInfo) {
    /**
     * Проверяет сообщение пользователя на соответствие команде
     *
     * @param command корректная команда, с которой сравнивается сообщение
     * @param message сообщение пользователя
     * @return {@code true} - если сообщение соответствует команде, иначе - {@code false}
     */
    public static boolean isValidCommand(Command command, String message) {
        if (command.needExtraInfo) {
            String[] splittedMessage = message.split(" ", 2);
            return splittedMessage.length == 2 && splittedMessage[0].equals(command.commandName);
        }
        return message.equals(command.commandName);
    }
}
