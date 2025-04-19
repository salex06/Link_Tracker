package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
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

class ListByTagMessageHandlerTest {
    private int port = 8089;

    @Autowired
    private static RestClient restClient;

    private WireMockServer wireMockServer;

    private static RedisCacheService redisCacheService;

    private static ListByTagMessageHandler listMessageHandler;

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

    @BeforeAll
    public static void setUp() {
        redisCacheService = Mockito.mock(RedisCacheService.class);
        listMessageHandler = new ListByTagMessageHandler(redisCacheService);
    }

    @Test
    public void handle_WhenCorrectRequestAndNoCachedInfo_ThenReturnListOfTrackedResources() {
        String expectedMessage =
                """
            Количество отслеживаемых ресурсов (для тега tag1): 2
            1) https://example1.com/
            2) https://example2.com/
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("tag1");
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        ListByTagMessageHandler listMessageHandler = new ListByTagMessageHandler(mock);
        stubFor(
                get("/linksbytag")
                        .withHeader("Tg-Chat-Id", equalTo("1"))
                        .withHeader("Tag-Value", equalTo("tag1"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                            {
                                "links":[
                                    {
                                        "id":1,
                                        "url":"https://example1.com/",
                                        "tags":["tag1", "tag2"],
                                        "filters":[]},
                                    {
                                        "id":2,
                                        "url":"https://example2.com/",
                                        "tags":["tag1", "tag3"],
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
            Количество отслеживаемых ресурсов (для тега tag): 0
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        String tagValue = "tag";
        when(message.text()).thenReturn("/list " + tagValue);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        ListByTagMessageHandler listMessageHandler = new ListByTagMessageHandler(mock);
        stubFor(
                get("/linksbytag")
                        .withHeader("Tg-Chat-Id", equalTo("1"))
                        .withHeader("Tag-Value", equalTo(tagValue))
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
    public void handle_WhenNoPassedIdOrTagAndNoCachedInfo_ThenReturnErrorMessage() {
        String expectedMessage = """
            Некорректные параметры запроса""";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(null);
        String tagValue = "tag";
        when(message.text()).thenReturn("/list " + tagValue);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        ListByTagMessageHandler listMessageHandler = new ListByTagMessageHandler(mock);
        stubFor(
                get("/linksbytag")
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
    public void handle_WhenWrongIdOrTagPassedAndNoCachedInfo_ThenReturnErrorMessage() {
        String expectedMessage = """
            Некорректные параметры запроса""";
        Long chatId = 50L;
        String tagValue = "tag";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/list " + tagValue);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(null);
        ListByTagMessageHandler listMessageHandler = new ListByTagMessageHandler(mock);
        stubFor(
                get("/linksbytag")
                        .withHeader("Tg-Chat-Id", equalTo(chatId.toString()))
                        .withHeader("Tag-Value", equalTo(tagValue))
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
        Количество отслеживаемых ресурсов (для тега tag1): 2
        1) https://example1.com/
        2) https://example2.com/
        """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("tag1");
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(expectedMessage);
        ListByTagMessageHandler listMessageHandler = new ListByTagMessageHandler(mock);
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
            Количество отслеживаемых ресурсов (для тега tag): 0
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        String tagValue = "tag";
        when(message.text()).thenReturn("/list " + tagValue);
        RedisCacheService mock = Mockito.mock(RedisCacheService.class);
        when(mock.getValue(any())).thenReturn(expectedMessage);
        ListByTagMessageHandler listMessageHandler = new ListByTagMessageHandler(mock);
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        restClient = Mockito.spy();

        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
        verify(mock, times(0)).putValue(any(), any());
        verify(restClient, times(0)).get();
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/listbytag", true);

        boolean result = listMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/somethingElse", false);

        boolean result = listMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }
}
