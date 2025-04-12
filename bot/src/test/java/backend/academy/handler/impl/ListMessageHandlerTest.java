package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import backend.academy.service.RedisCacheService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class ListMessageHandlerTest {
    private int port = 8089;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setupBeforeEach() {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
    }

    @AfterEach
    void shutdown() {
        wireMockServer.stop();
    }

    private static ListMessageHandler listMessageHandler;

    @BeforeAll
    public static void setUp() {
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        listMessageHandler = new ListMessageHandler(mock);
    }

    @Test
    public void handle_WhenCorrectRequestAndNoCachedInfo_ThenReturnListOfTrackedResources() {
        String expectedMessage =
                """
                Количество отслеживаемых ресурсов: 2
                1) https://example1.com/
                Теги: tag1	tag2\t
                2) https://example2.com/
                Теги: tag2	tag3\t
                """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        listMessageHandler = new ListMessageHandler(mock);
        stubFor(
                get("/links")
                        .withHeader("Tg-Chat-Id", equalTo("1"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                    "links":[
                                                        {
                                                            "id":1,"url":"https://example1.com/",
                                                            "tags":["tag1", "tag2"],
                                                            "filters":[]},
                                                        {
                                                            "id":2,
                                                            "url":"https://example2.com/",
                                                            "tags":["tag2", "tag3"],
                                                            "filters":[]
                                                        }
                                                    ],
                                                    "size":2
                                                }
                                            """)));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenCorrectRequestAndNoTrackedLinksAndNoCachedInfo_ThenReturnEmptyLinkList() {
        String expectedMessage = """
            Количество отслеживаемых ресурсов: 0
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        listMessageHandler = new ListMessageHandler(mock);
        stubFor(
                get("/links")
                        .withHeader("Tg-Chat-Id", equalTo("1"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            {
                                "links":[],
                                "size":0
                            }
                            """)));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenNoPassedIdAndNoCachedInfo_ThenReturnErrorMessage() {
        String expectedMessage = """
            Некорректные параметры запроса""";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(null);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        listMessageHandler = new ListMessageHandler(mock);
        stubFor(
                get("/links")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                "description":"Некорректные параметры запроса","code":"400",
                                                "exceptionName":"MissingRequestHeaderException", "exceptionMessage":
                                                "Required request header 'Tg-Chat-Id' for method parameter type Long is not present",
                                                "stacktrace": []
                                                }
                                            """)));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenWrongIdPassedAndNoCachedInfo_ThenReturnErrorMessage() {
        String expectedMessage = """
            Некорректные параметры запроса""";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        listMessageHandler = new ListMessageHandler(mock);
        stubFor(
                get("/links")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                {
                                    "description":"Некорректные параметры запроса",
                                    "code":"400",
                                    "exceptionName":"",
                                    "exceptionMessage": "",
                                    "stacktrace": []
                                }
                            """)));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenCorrectRequestAndCachedInfoExists_ThenReturnCachedListOfTrackedResources() {
        String expectedMessage =
                """
            Количество отслеживаемых ресурсов: 2
            1) https://example1.com/
            Теги: tag1	tag2\t
            2) https://example2.com/
            Теги: tag2	tag3\t
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(expectedMessage);
        listMessageHandler = new ListMessageHandler(mock);
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        restClient = Mockito.spy();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        verify(mock, times(0)).putValue(any(), any());
        verify(restClient, times(0)).get();
    }

    @Test
    public void handle_WhenCorrectRequestAndNoTrackedLinksAndCachedInfoExists_ThenReturnCachedEmptyLinkList() {
        String expectedMessage = """
            Количество отслеживаемых ресурсов: 0
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(expectedMessage);
        listMessageHandler = new ListMessageHandler(mock);
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        restClient = Mockito.spy();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        verify(mock, times(0)).putValue(any(), any());
        verify(restClient, times(0)).get();
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/list", false);

        boolean result = listMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/somethingElse", true);

        boolean result = listMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }
}
