package backend.academy.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultMessageHandlerTest {
    private final DefaultMessageHandler defaultMessageHandler = new DefaultMessageHandler();

    @Test
    void handleMessage() {
        String command = "test";
        String expectedMessage = "Неизвестная команда: " + command;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(command);
        when(message.chat()).thenReturn(chat);

        SendMessage actualSendMessage = defaultMessageHandler.handle(update, null);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    void supportCommandReturnsTrue() {
        Command command = new Command("/anycomand", false);

        boolean result = defaultMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }
}
