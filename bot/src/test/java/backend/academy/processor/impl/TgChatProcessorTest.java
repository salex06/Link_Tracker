package backend.academy.processor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.bot.commands.Command;
import backend.academy.crawler.CrawlerManager;
import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.MessageCrawler;
import backend.academy.crawler.impl.TrackMessageCrawler;
import backend.academy.crawler.impl.tags.add.AddTagMessageCrawler;
import backend.academy.handler.Handler;
import backend.academy.handler.HandlerManager;
import backend.academy.handler.impl.AddTagMessageHandler;
import backend.academy.handler.impl.DefaultMessageHandler;
import backend.academy.handler.impl.ListMessageHandler;
import backend.academy.handler.impl.StartMessageHandler;
import backend.academy.handler.impl.TrackMessageHandler;
import backend.academy.handler.impl.UntrackMessageHandler;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class TgChatProcessorTest {
    private static DefaultMessageHandler defaultMessageHandler;
    private static StartMessageHandler startMessageHandler;
    private static TrackMessageHandler trackMessageHandler;
    private static UntrackMessageHandler untrackMessageHandler;
    private static ListMessageHandler listMessageHandler;
    private static TrackMessageCrawler trackMessageCrawler;
    private static AddTagMessageCrawler addTagMessageCrawler;
    private static AddTagMessageHandler addTagMessageHandler;
    private static CrawlerManager crawlerManager;

    private static List<Map.Entry<MessageCrawler, Handler>> pairs;

    @Mock
    private static HandlerManager handlerManager;

    @Mock
    private RestClient restClient;

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
        addTagMessageHandler = Mockito.mock(AddTagMessageHandler.class);

        trackMessageCrawler = Mockito.mock(TrackMessageCrawler.class);
        addTagMessageCrawler = Mockito.mock(AddTagMessageCrawler.class);
        pairs = List.of(
                Map.entry(trackMessageCrawler, trackMessageHandler),
                Map.entry(addTagMessageCrawler, addTagMessageHandler));
        crawlerManager = Mockito.mock(CrawlerManager.class);
        when(crawlerManager.getCrawlerHandlerPair()).thenReturn(pairs);

        doReturn(new SendMessage(123, "Неизвестная команда"))
                .when(defaultMessageHandler)
                .handle(any(Update.class), any(RestClient.class));
        doReturn(new SendMessage(123, "Ссылка добавлена"))
                .when(trackMessageHandler)
                .handle(any(Update.class), any(RestClient.class));
        doReturn(new SendMessage(123, "Пользователь зарегистрирован"))
                .when(startMessageHandler)
                .handle(any(Update.class), any(RestClient.class));
        doReturn(new SendMessage(123, "Ссылка удалена"))
                .when(untrackMessageHandler)
                .handle(any(Update.class), any(RestClient.class));
        doReturn(new SendMessage(123L, "Список ссылок"))
                .when(listMessageHandler)
                .handle(any(Update.class), any(RestClient.class));
        doReturn(new SendMessage(1L, "Тег успешно добавлен"))
                .when(addTagMessageHandler)
                .handle(any(Update.class), any(RestClient.class));
    }

    @Test
    void processStartMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            when(trackMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
            when(addTagMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
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
            DialogStateDTO dialogStateDTO = Mockito.mock(DialogStateDTO.class);
            when(dialogStateDTO.message()).thenReturn(new SendMessage(1, "Введите ссылку:"));
            when(trackMessageCrawler.crawl(any(Update.class))).thenReturn(dialogStateDTO);
            when(addTagMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));

            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Введите ссылку:";
            when(message.text()).thenReturn("/track");

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }

    @Test
    void processUndefinedMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            when(trackMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
            when(addTagMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
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
            when(trackMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
            when(addTagMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
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
            when(trackMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
            when(addTagMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
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

    @Test
    void processAddTagMessage() {
        try (MockedStatic<BotCommandsStorage> botCommandsStorageMockedStatic = mockStatic(BotCommandsStorage.class)) {
            when(trackMessageCrawler.crawl(any(Update.class))).thenReturn(new DialogStateDTO(null, false));
            DialogStateDTO dialogStateDTO = Mockito.mock(DialogStateDTO.class);
            when(dialogStateDTO.message())
                    .thenReturn(new SendMessage(1, "Введите ссылку для тега (ALL - добавить тег для всех ссылок):"));
            when(addTagMessageCrawler.crawl(any(Update.class))).thenReturn(dialogStateDTO);
            botCommandsStorageMockedStatic
                    .when(() -> BotCommandsStorage.getCommand(anyString()))
                    .thenReturn(command);
            String expectedMessage = "Введите ссылку для тега (ALL - добавить тег для всех ссылок):";
            when(message.text()).thenReturn("/addtag");

            SendMessage actualSendMessage = tgChatProcessor.process(update);

            assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        }
    }
}
