package backend.academy.bot.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BotCommandsStorageTest {
    @Test
    void returnAllAvailableCommands() {
        List<Command> expectedCommands = commands();

        List<Command> actualCommand = BotCommandsStorage.getCommands();

        assertEquals(expectedCommands, actualCommand);
    }

    private static List<Command> commands() {
        List<Command> commandList = new ArrayList<>();
        commandList.add(new Command("/start", false));
        commandList.add(new Command("/help", false));
        commandList.add(new Command("/list", false));
        commandList.add(new Command("/track", false));
        commandList.add(new Command("/untrack", true));
        return commandList;
    }

    @ParameterizedTest
    @MethodSource("correctMessageAndCommand")
    void getCommand_WhenCorrectMessage_ThenReturnCommand(String message, Command expectedCommand) {
        Command actualCommand = BotCommandsStorage.getCommand(message);

        assertEquals(expectedCommand, actualCommand);
    }

    private static List<Arguments> correctMessageAndCommand() {
        return List.of(
                Arguments.of("/start", new Command("/start", false)),
                Arguments.of("/help", new Command("/help", false)),
                Arguments.of("/list", new Command("/list", false)),
                Arguments.of("/track", new Command("/track", false)),
                Arguments.of("/untrack linkExample", new Command("/untrack", true)));
    }

    @Test
    void getCommand_WhenIncorrectMessage_ThenReturnNull() {
        String message = "test";

        Command command = BotCommandsStorage.getCommand(message);

        assertNull(command);
    }

    @Test
    void getCommandDescriptionReturnsCorrectMap() {
        Map<String, String> expectedMap = Map.of(
                "/start", "Запустить бота",
                "/help", "Вывести все команды на экран",
                "/track", "Запустить отслеживание ресурса по ссылке, следующей за командой",
                "/untrack", "Прекратить отслеживание ресурса по ссылке, следующей за командой",
                "/list", "Получить список всех отслеживаемых ресурсов");

        Map<String, String> actualMap = BotCommandsStorage.getCommandDescription();

        assertEquals(expectedMap, actualMap);
    }
}
