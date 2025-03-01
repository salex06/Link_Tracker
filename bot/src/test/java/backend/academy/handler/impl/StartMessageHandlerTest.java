package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
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

class StartMessageHandlerTest {
    private int port = 8089;

    @Autowired
    private static RestClient restClient;

    private static StartMessageHandler startMessageHandler;

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
        startMessageHandler = new StartMessageHandler();
    }

    @Test
    public void handle_WhenCorrectRequest_ThenReturnSuccessMessage() {
        String expectedMessage = "Вы зарегистрированы";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        stubFor(post("/tg-chat/" + chat.id())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withHeader("charset", "utf-8")
                        .withBody("Вы зарегистрированы")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualSendMessage = startMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenAlreadyRegistered_ThenReturnError() {
        String expectedMessage = "Некорректные параметры запроса";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        stubFor(post("/tg-chat/" + chat.id())
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                + "\"exceptionName\":\"\", \"exceptionMessage\": \"\", \"stacktrace\": []}")));

        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        SendMessage actualSendMessage = startMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualSendMessage.getParameters().get("text"));
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/start", false);

        boolean result = startMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/anyCommand", true);

        boolean result = startMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }
}
