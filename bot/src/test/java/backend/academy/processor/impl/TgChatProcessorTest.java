package backend.academy.processor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.bot.commands.Command;
import backend.academy.handler.HandlerManager;
import backend.academy.handler.impl.DefaultMessageHandler;
import backend.academy.handler.impl.ListMessageHandler;
import backend.academy.handler.impl.StartMessageHandler;
import backend.academy.handler.impl.TrackMessageHandler;
import backend.academy.handler.impl.UntrackMessageHandler;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TgChatProcessorTest {
    private static DefaultMessageHandler defaultMessageHandler;
    private static StartMessageHandler startMessageHandler;
    private static TrackMessageHandler trackMessageHandler;
    private static UntrackMessageHandler untrackMessageHandler;
    private static ListMessageHandler listMessageHandler;

    @Mock
    private static HandlerManager handlerManager;

    @InjectMocks
    private TgChatProcessor tgChatProcessor;

    @Mock
    private static Command command;

    private static Update update;
    private static Message message;

    @BeforeAll
    static void setUp() {
        update = Mockito.mock(Update.class);
        message = Mockito.mock(Message.class);
        when(update.message()).thenReturn(message);

        defaultMessageHandler = Mockito.mock(DefaultMessageHandler.class);
        startMessageHandler = Mockito.mock(StartMessageHandler.class);
        trackMessageHandler = Mockito.mock(TrackMessageHandler.class);
        untrackMessageHandler = Mockito.mock(UntrackMessageHandler.class);
        listMessageHandler = Mockito.mock(ListMessageHandler.class);
        doReturn(new SendMessage(123, "Неизвестная команда"))
                .when(defaultMessageHandler)
                .handle(any(Update.class));
        doReturn(new SendMessage(123, "Ссылка добавлена"))
                .when(trackMessageHandler)
                .handle(any(Update.class));
        doReturn(new SendMessage(123, "Пользователь зарегистрирован"))
                .when(startMessageHandler)
                .handle(any(Update.class));
        doReturn(new SendMessage(123, "Ссылка удалена"))
                .when(untrackMessageHandler)
                .handle(any(Update.class));
        doReturn(new SendMessage(123L, "Список ссылок"))
                .when(listMessageHandler)
                .handle(any(Update.class));
    }

    @Test
    void processStartMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Пользователь зарегистрирован";
            when(message.text()).thenReturn("/start");
            when(handlerManager.manageHandler(any())).thenReturn(startMessageHandler);

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }

    @Test
    void processTrackMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Ссылка добавлена";
            when(message.text()).thenReturn("/track abc");
            when(handlerManager.manageHandler(any())).thenReturn(trackMessageHandler);

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }

    @Test
    void processUndefinedMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Неизвестная команда";
            when(message.text()).thenReturn("/undefined command");
            when(handlerManager.manageHandler(any())).thenReturn(defaultMessageHandler);

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }

    @Test
    void processUntrackMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Ссылка удалена";
            when(message.text()).thenReturn("/untrack abc");
            when(handlerManager.manageHandler(any())).thenReturn(untrackMessageHandler);

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }

    @Test
    void processListMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Список ссылок";
            when(message.text()).thenReturn("/list");
            when(handlerManager.manageHandler(any())).thenReturn(listMessageHandler);

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }
}
