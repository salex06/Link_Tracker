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
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.config.properties.CircuitBreakerDefaultProperties;
import backend.academy.config.properties.RetryDefaultProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class TimeConfigurationSettingsHandlerTest {
    private int port;

    @Autowired
    private static RestClient restClient;

    @Autowired
    private TimeConfigurationSettingsHandler timeConfigurationSettingsHandler;

    @Autowired
    private ApplicationStabilityProperties stabilityProperties;

    private WireMockServer wireMockServer;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryDefaultProperties retryDefaultProperties;

    @BeforeEach
    public void setupBeforeEach() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        port = wireMockServer.port();
        WireMock.configureFor("localhost", port);

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("default");
        circuitBreaker.reset();
    }

    @AfterEach
    public void shutdown() {
        wireMockServer.stop();
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
                post("/timeconfig")
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
        String expectedMessage = "Ошибка. Не удалось выполнить запрос :(";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/timeconfig 09:31");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(retryDefaultProperties.getMaxAttempts());
    }

    @ParameterizedTest
    @MethodSource("getAllowedHttpCodes")
    void handle_whenSuccessAfterRetryAndAllowedHttpCode_ThenReturnLinkResponse(Integer httpCode) throws Exception {
        setupStubForRetry_LastSuccessful(httpCode);
        String expectedMessage = "Конфигурация времени отправки уведомлений прошла успешно!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/timeconfig 14:55");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(retryDefaultProperties.getMaxAttempts());
    }

    @Test
    void handle_whenUnexpectedErrorCode_ThenRecoveryCalledWithoutRetrying() throws Exception {
        String expectedMessage = "Ошибка. Не удалось выполнить запрос :(";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/timeconfig 10:35");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                .inScenario("Timeconfig_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(404))
                .willSetStateTo(""));

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(1);
    }

    public void setupStubForRetry_AllFailed(int httpCode) {
        int maxAttempts = retryDefaultProperties.getMaxAttempts();

        WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                .inScenario("Timeconfig_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(httpCode))
                .willSetStateTo("Attempt 1"));

        for (int i = 0; i < maxAttempts - 2; ++i) {
            WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                    .inScenario("Timeconfig_Retry")
                    .whenScenarioStateIs("Attempt " + (i + 1))
                    .willReturn(WireMock.aResponse().withStatus(httpCode))
                    .willSetStateTo("Attempt " + (i + 2)));
        }

        WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                .inScenario("Timeconfig_Retry")
                .whenScenarioStateIs("Attempt " + (maxAttempts - 1))
                .willReturn(WireMock.aResponse().withStatus(httpCode).withBody(""))
                .willSetStateTo(""));
    }

    public void setupStubForRetry_LastSuccessful(int httpCode) {
        int maxAttempts = retryDefaultProperties.getMaxAttempts();

        WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                .inScenario("Timeconfig_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(httpCode))
                .willSetStateTo("Attempt 1"));

        for (int i = 0; i < maxAttempts - 2; ++i) {
            WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                    .inScenario("Timeconfig_Retry")
                    .whenScenarioStateIs("Attempt " + (i + 1))
                    .willReturn(WireMock.aResponse().withStatus(httpCode))
                    .willSetStateTo("Attempt " + (i + 2)));
        }

        WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                .inScenario("Timeconfig_Retry")
                .whenScenarioStateIs("Attempt " + (maxAttempts - 1))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withBody("Конфигурация времени отправки уведомлений прошла успешно!"))
                .willSetStateTo(""));
    }

    public void verifyNumberOfCall(Integer numberOfCall) {
        WireMock.verify(numberOfCall, postRequestedFor(urlEqualTo("/timeconfig")));
    }

    @Autowired
    private SimpleClientHttpRequestFactory requestFactory;

    @Autowired
    private CircuitBreakerDefaultProperties circuitBreakerProperties;

    @Test
    public void handle_WhenTheNumberOfFailAttemptsExceededTheLimit_ThenReturnErrorSendMessage()
            throws InterruptedException {
        int numberOfCalls = circuitBreakerProperties.getMinimumNumberOfCalls();
        int timeout = stabilityProperties.getTimeout().getConnectTimeout()
                + stabilityProperties.getTimeout().getReadTimeout();
        WireMock.stubFor(WireMock.post(urlEqualTo("/timeconfig"))
                .willReturn(WireMock.aResponse().withStatus(200).withFixedDelay(timeout + 200)));
        String expectedMessage = "Ошибка. Сервис недоступен, попробуйте позже :(";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/timeconfig 10:30");
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .requestFactory(requestFactory)
                .build();

        for (int i = 0; i < numberOfCalls; ++i) {
            SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);
            assertNotEquals(expectedMessage, actualMessage.getParameters().get("text"));
        }

        SendMessage actualMessage = timeConfigurationSettingsHandler.handle(update, restClient);
        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }
}
