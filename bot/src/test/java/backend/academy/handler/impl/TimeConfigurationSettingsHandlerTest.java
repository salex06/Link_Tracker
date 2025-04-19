package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class TimeConfigurationSettingsHandlerTest {
    private int port;

    @Autowired
    private static RestClient restClient;

    private TimeConfigurationSettingsHandler timeConfigurationSettingsHandler;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setupBeforeEach() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        port = wireMockServer.port();
        WireMock.configureFor("localhost", port);
    }

    @AfterEach
    public void shutdown() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void setUp() {
        timeConfigurationSettingsHandler = new TimeConfigurationSettingsHandler();
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/timeconfig", true);

        boolean result = timeConfigurationSettingsHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/anyCommand", false);

        boolean result = timeConfigurationSettingsHandler.supportCommand(command);

        assertThat(result).isFalse();
    }

    @Test
    public void handle_WhenHeaderWasNotPassed_ThenReturnError() {
        String expectedMessage = "Некорректные параметры запроса";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/timeconfig 10:00");
        stubFor(
                patch("/timeconfig")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"stacktrace\": []}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenCommandIsWrong_ThenReturnError() {
        String expectedMessage = "Ошибка! Попробуйте снова";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/timeconfig wrongConfig");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }

    @Test
    public void handle_WhenCorrectRequest_ThenReturnLinkResponse() {
        String expectedMessage = "Конфигурация времени отправки уведомлений прошла успешно!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/timeconfig 10:00");
        stubFor(post("/timeconfig")
                .willReturn(aResponse()
                        .withHeader("Tg-Chat-Id", chat.id().toString())
                        .withHeader("Time-Config", "10:00")
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }
}
