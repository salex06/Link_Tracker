package backend.academy.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import backend.academy.handler.impl.DefaultMessageHandler;
import backend.academy.handler.impl.HelpMessageHandler;
import backend.academy.handler.impl.ListMessageHandler;
import backend.academy.handler.impl.StartMessageHandler;
import backend.academy.handler.impl.TrackMessageHandler;
import backend.academy.handler.impl.UntrackMessageHandler;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@ExtendWith(MockitoExtension.class)
class HandlerManagerTest {
    private static DefaultMessageHandler defaultMessageHandler;
    private static StartMessageHandler startMessageHandler;
    private static TrackMessageHandler trackMessageHandler;
    private static UntrackMessageHandler untrackMessageHandler;
    private static ListMessageHandler listMessageHandler;
    private static HelpMessageHandler helpMessageHandler;

    private static List<Handler> handlers;

    private static HandlerManager handlerManager;

    @BeforeAll
    static void setUp() {
        defaultMessageHandler = Mockito.mock(DefaultMessageHandler.class);
        startMessageHandler = Mockito.mock(StartMessageHandler.class);
        trackMessageHandler = Mockito.mock(TrackMessageHandler.class);
        untrackMessageHandler = Mockito.mock(UntrackMessageHandler.class);
        listMessageHandler = Mockito.mock(ListMessageHandler.class);
        helpMessageHandler = Mockito.mock(HelpMessageHandler.class);

        handlers = List.of(
                defaultMessageHandler,
                startMessageHandler,
                trackMessageHandler,
                untrackMessageHandler,
                listMessageHandler,
                helpMessageHandler);

        handlerManager = new HandlerManager(handlers);
    }

    @Test
    void manageHandler_WhenStartCommand_ThenReturnHandler() {
        Command command = new Command("/start", false);
        Class<?> expectedClassType = startMessageHandler.getClass();
        when(startMessageHandler.supportCommand(command)).thenReturn(true);

        Handler actualHandler = handlerManager.manageHandler(command);

        assertThat(actualHandler).isInstanceOf(expectedClassType);
    }

    @Test
    void manageHandler_WhenTrackCommand_ThenReturnHandler() {
        Command command = new Command("/track", true);
        Class<?> expectedClassType = trackMessageHandler.getClass();
        when(trackMessageHandler.supportCommand(command)).thenReturn(true);

        Handler actualHandler = handlerManager.manageHandler(command);

        assertThat(actualHandler).isInstanceOf(expectedClassType);
    }

    @Test
    void manageHandler_WhenUntrackCommand_ThenReturnHandler() {
        Command command = new Command("/untrack", true);
        Class<?> expectedClassType = untrackMessageHandler.getClass();
        when(untrackMessageHandler.supportCommand(command)).thenReturn(true);

        Handler actualHandler = handlerManager.manageHandler(command);

        assertThat(actualHandler).isInstanceOf(expectedClassType);
    }

    @Test
    void manageHandler_WhenHelpCommand_ThenReturnHandler() {
        Command command = new Command("/help", false);
        Class<?> expectedClassType = helpMessageHandler.getClass();
        when(helpMessageHandler.supportCommand(command)).thenReturn(true);

        Handler actualHandler = handlerManager.manageHandler(command);

        assertThat(actualHandler).isInstanceOf(expectedClassType);
    }

    @Test
    void manageHandler_WhenListCommand_ThenReturnHandler() {
        Command command = new Command("/list", false);
        Class<?> expectedClassType = listMessageHandler.getClass();
        when(listMessageHandler.supportCommand(command)).thenReturn(true);

        Handler actualHandler = handlerManager.manageHandler(command);

        assertThat(actualHandler).isInstanceOf(expectedClassType);
    }

    @Test
    void manageHandler_WhenNullCommand_ThenReturnHandler() {
        Command command = null;
        Class<?> expectedClassType = defaultMessageHandler.getClass();
        when(defaultMessageHandler.supportCommand(command)).thenReturn(true);

        Handler actualHandler = handlerManager.manageHandler(command);

        assertThat(actualHandler).isInstanceOf(expectedClassType);
    }
}
