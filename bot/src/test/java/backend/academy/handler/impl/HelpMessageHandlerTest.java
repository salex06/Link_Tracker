package backend.academy.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HelpMessageHandlerTest {
    private static Map<String, String> testCommandDescription;

    private static HelpMessageHandler helpMessageHandler;

    @BeforeAll
    public static void setUp() {
        testCommandDescription = new LinkedHashMap<>();
        testCommandDescription.put("/command1", "command1 descr");
        testCommandDescription.put("/command2", "command2 descr");
        testCommandDescription.put("/command3", "command3 descr");

        helpMessageHandler = new HelpMessageHandler(testCommandDescription);
    }

    @Test
    public void handleReturnsDescriptionOfCommands() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        String expectedDescription =
                """
            /command1 - command1 descr
            /command2 - command2 descr
            /command3 - command3 descr
            """;

        SendMessage actualSendMessage = helpMessageHandler.handle(update, null);

        assertEquals(expectedDescription, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/help", false);

        boolean result = helpMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/wrongCommand", false);

        boolean result = helpMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }
}
