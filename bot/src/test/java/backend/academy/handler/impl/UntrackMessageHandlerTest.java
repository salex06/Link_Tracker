package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
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

class UntrackMessageHandlerTest {
    private int port = 8089;

    @Autowired
    private static RestClient restClient;

    private static UntrackMessageHandler untrackMessageHandler;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setupBeforeEach() {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
    }

    @AfterEach
    public void shutdown() {
        wireMockServer.stop();
    }

    @BeforeAll
    public static void setUp() {
        untrackMessageHandler = new UntrackMessageHandler();
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/untrack", true);

        boolean result = untrackMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/somethingElse", true);

        boolean result = untrackMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }

    @Test
    public void handle_WhenMessageIsWrong_ThenReturnError() {
        String expectedMessage = "Вы должны указать URL после команды!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("somethingWrong");

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage sendMessage = untrackMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, sendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenHeaderWasNotPassed_ThenReturnError() {
        String expectedMessage = "Некорректные параметры запроса";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(null);
        when(message.text()).thenReturn("/untrack linkExample");

        stubFor(
                delete("/links")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"stacktrace\": []}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualMessage = untrackMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenWrongTgChatId_ThenReturnError() {
        String expectedMessage = "Некорректные параметры запроса";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/untrack linkExample");

        stubFor(delete("/links")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                + "\"exceptionName\":\"\", \"exceptionMessage\": \"\", \"stacktrace\": []}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualMessage = untrackMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenCorrectRequest_ThenReturnLinkResponse() {
        String expectedMessage = String.format("Ресурс %s удален из отслеживаемых. ID: %d", "linkExample", 1);
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/untrack linkExample");

        stubFor(delete("/links")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1,\"url\":\"linkExample\"}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualMessage = untrackMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenWrongLink_ThenReturnNotFoundMessage() {
        String expectedMessage = "Ссылка не найдена";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/untrack linkExample");

        stubFor(delete("/links")
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"description\":\"Ссылка не найдена\",\"code\":\"404\", "
                                + "\"exceptionName\":\"\", \"exceptionMessage\": \"\", \"stacktrace\": []}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualMessage = untrackMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }
}
