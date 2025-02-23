package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class ListMessageHandlerTest {
    private int port = 8089;

    @Autowired
    private static RestClient restClient;

    @Autowired
    @Mock
    TelegramBot telegramBot;

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
        listMessageHandler = new ListMessageHandler();
    }

    @Test
    public void handle_WhenCorrectRequest_ThenReturnListOfTrackedResources() {
        String expectedMessage =
                """
            Количество отслеживаемых ресурсов: 2
            1) https://example1.com/
            2) https://example2.com/
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        stubFor(
                get("/links")
                        .withHeader("Tg-Chat-Id", equalTo("1"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"links\":[{\"id\":1,\"url\":\"https://example1.com/\"},{\"id\":2,\"url\":\"https://example2.com/\"}],\"size\":2}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenCorrectRequestAndNoTrackedLinks_ThenReturnEmptyLinkList() {
        String expectedMessage = """
            Количество отслеживаемых ресурсов: 0
            """;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        stubFor(get("/links")
                .withHeader("Tg-Chat-Id", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"links\":[],\"size\":0}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenNoPassedId_ThenReturnErrorMessage() {
        String expectedMessage = """
            Некорректные параметры запроса""";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(null);

        stubFor(
                get("/links")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"trace\": []}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenWrongIdPassed_ThenReturnErrorMessage() {
        String expectedMessage = """
            Некорректные параметры запроса""";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);

        stubFor(get("/links")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                + "\"exceptionName\":\"\", \"exceptionMessage\": \"\", \"trace\": []}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualSendMessage = listMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
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
