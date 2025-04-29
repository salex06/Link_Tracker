package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class StartMessageHandlerTest {
    private int port = 8089;

    @Autowired
    private static RestClient restClient;

    @Autowired
    private RetryTemplate retryTemplate;

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

    @BeforeEach
    public void setUp() {
        startMessageHandler = new StartMessageHandler(retryTemplate);
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

    @Autowired
    private ApplicationStabilityProperties properties;

    private List<Integer> getAllowedHttpCodes() {
        return properties.getRetry().getHttpCodes();
    }

    @ParameterizedTest
    @MethodSource("getAllowedHttpCodes")
    void handle_whenMaxRetriesExceededAndAllowedHttpCode_ThenRecoveryCalledAfterRetries(Integer httpCode)
            throws Exception {
        setupStubForRetry_AllFailed(httpCode);
        String expectedMessage = "Ошибка при регистрации";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("/start");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = startMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(properties.getRetry().getMaxAttempts());
    }

    @ParameterizedTest
    @MethodSource("getAllowedHttpCodes")
    void handle_whenSuccessAfterRetryAndAllowedHttpCode_ThenReturnLinkResponse(Integer httpCode) throws Exception {
        setupStubForRetry_LastSuccessful(httpCode);
        String expectedMessage = "Вы зарегистрированы";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("/start");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = startMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(properties.getRetry().getMaxAttempts());
    }

    @Test
    void handle_whenUnexpectedErrorCode_ThenRecoveryCalledWithoutRetrying() throws Exception {
        String expectedMessage = "Ошибка при регистрации";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("/start");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                .inScenario("Start_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(404))
                .willSetStateTo(""));

        SendMessage actualMessage = startMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(1);
    }

    public void setupStubForRetry_AllFailed(int httpCode) {
        int maxAttempts = properties.getRetry().getMaxAttempts();

        WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                .inScenario("Start_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(httpCode))
                .willSetStateTo("Attempt 1"));

        for (int i = 0; i < maxAttempts - 2; ++i) {
            WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                    .inScenario("Start_Retry")
                    .whenScenarioStateIs("Attempt " + (i + 1))
                    .willReturn(WireMock.aResponse().withStatus(httpCode))
                    .willSetStateTo("Attempt " + (i + 2)));
        }

        WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                .inScenario("Start_Retry")
                .whenScenarioStateIs("Attempt " + (maxAttempts - 1))
                .willReturn(WireMock.aResponse().withStatus(httpCode).withBody(""))
                .willSetStateTo(""));
    }

    public void setupStubForRetry_LastSuccessful(int httpCode) {
        int maxAttempts = properties.getRetry().getMaxAttempts();

        WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                .inScenario("Start_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(httpCode))
                .willSetStateTo("Attempt 1"));

        for (int i = 0; i < maxAttempts - 2; ++i) {
            WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                    .inScenario("Start_Retry")
                    .whenScenarioStateIs("Attempt " + (i + 1))
                    .willReturn(WireMock.aResponse().withStatus(httpCode))
                    .willSetStateTo("Attempt " + (i + 2)));
        }

        WireMock.stubFor(WireMock.post(urlEqualTo("/tg-chat/1"))
                .inScenario("Start_Retry")
                .whenScenarioStateIs("Attempt " + (maxAttempts - 1))
                .willReturn(WireMock.aResponse().withStatus(200).withBody("Вы зарегистрированы"))
                .willSetStateTo(""));
    }

    public void verifyNumberOfCall(Integer numberOfCall) {
        WireMock.verify(numberOfCall, postRequestedFor(urlEqualTo("/tg-chat/1")));
    }
}
