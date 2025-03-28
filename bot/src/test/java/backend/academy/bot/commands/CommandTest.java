package backend.academy.bot.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommandTest {
    @Test
    void testRecordConstructor() {
        String expectedCommandName = "command";
        boolean expectedNeedExtraInfo = false;

        Command command = new Command(expectedCommandName, expectedNeedExtraInfo);

        assertEquals(expectedCommandName, command.commandName());
        assertEquals(expectedNeedExtraInfo, command.needExtraInfo());
    }

    @ParameterizedTest
    @MethodSource("isValidCommand_correctData")
    void isValidCommand_WhenCorrectMessage_ThenReturnTrue(String message, Command command) {
        boolean isValidCommand = Command.isValidCommand(command, message);

        assertTrue(isValidCommand);
    }

    private static List<Arguments> isValidCommand_correctData() {
        return List.of(
                Arguments.of("commandName extraInfo", new Command("commandName", true)),
                Arguments.of("commandName", new Command("commandName", false)));
    }

    @Test
    void isValidCommand_WhenWrongMessage_ThenReturnFalse() {
        Command command = new Command("commandName", false);
        String message = "something wrong";

        boolean isValidCommand = Command.isValidCommand(command, message);

        assertFalse(isValidCommand);
    }
}
