package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.config.properties.CircuitBreakerDefaultProperties;
import backend.academy.config.properties.RetryDefaultProperties;
import backend.academy.crawler.impl.tags.removetag.RemoveTagMessageCrawler;
import backend.academy.dto.AddLinkRequest;
import backend.academy.service.RedisCacheService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.ArrayList;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class RemoveTagMessageHandlerTest {
    private int port;
    private WireMockServer wireMockServer;

    @Autowired
    private static RestClient restClient;

    @Autowired
    private RemoveTagMessageHandler removeTagMessageHandler;

    @MockitoBean
    private RemoveTagMessageCrawler crawler;

    @MockitoBean
    private RedisCacheService mockedRedisService;

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
        Command command = new Command("/removetag", true);

        boolean result = removeTagMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/anyCommand", false);

        boolean result = removeTagMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }

    @Test
    public void handle_WhenHeaderWasNotPassed_ThenReturnError() {
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("link", List.of("tag"), new ArrayList<>()));
        String expectedMessage = "Некорректные параметры запроса";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/removetag tagExample");
        stubFor(
                delete("/removetag")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"stacktrace\": []}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
    }

    @Test
    public void handle_WhenCrawlerReportIsNull_ThenReturnError() {
        String expectedMessage = "Ошибка, попробуйте снова";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/removetag tagExample");
        stubFor(
                delete("/removetag")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"stacktrace\": []}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
    }

    @Test
    public void handle_WhenCorrectRequestAndRemoveToAll_ThenReturnLinkResponse() {
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("linkExample", List.of("tag"), null));
        String expectedMessage = "Тег успешно удален!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/removetag tagExample");
        stubFor(delete("/removetag")
                .willReturn(aResponse()
                        .withHeader("Tg-Chat-Id", chat.id().toString())
                        .withHeader("Remove-To-All", "true")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": null,\"url\": null, \"tags\" : [\"tagExample\"], \"filters\":[]}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
    }

    @Test
    public void handle_WhenCorrectRequest_ThenReturnLinkResponse() {
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("link.link", List.of("tag"), null));
        String expectedMessage = "Тег успешно удален!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/removetag tagExample");
        stubFor(delete("/removetag")
                .willReturn(aResponse()
                        .withHeader("Tg-Chat-Id", chat.id().toString())
                        .withHeader("Remove-To-All", "false")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1,\"url\": \"linkExample\", \"tags\" : [], \"filters\":[]}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
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
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("linkExample", List.of("tag"), null));
        String expectedMessage = "Ошибка. Не удалось выполнить запрос :(";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/removetag tagExample");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(retryDefaultProperties.getMaxAttempts());
    }

    @ParameterizedTest
    @MethodSource("getAllowedHttpCodes")
    void handle_whenSuccessAfterRetryAndAllowedHttpCode_ThenReturnLinkResponse(Integer httpCode) throws Exception {
        setupStubForRetry_LastSuccessful(httpCode);
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("linkExample", List.of("tag"), null));
        String expectedMessage = "Тег успешно удален!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/removetag tagExample");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(retryDefaultProperties.getMaxAttempts());
    }

    @Test
    void handle_whenUnexpectedErrorCode_ThenRecoveryCalledWithoutRetrying() throws Exception {
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("linkExample", List.of("tag"), null));
        String expectedMessage = "Ошибка. Не удалось выполнить запрос :(";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/removetag tagExample");
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                .inScenario("RemoveTag_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(404))
                .willSetStateTo(""));

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verifyNumberOfCall(1);
    }

    public void setupStubForRetry_AllFailed(int httpCode) {
        int maxAttempts = retryDefaultProperties.getMaxAttempts();

        WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                .inScenario("RemoveTag_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(httpCode))
                .willSetStateTo("Attempt 1"));

        for (int i = 0; i < maxAttempts - 2; ++i) {
            WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                    .inScenario("RemoveTag_Retry")
                    .whenScenarioStateIs("Attempt " + (i + 1))
                    .willReturn(WireMock.aResponse().withStatus(httpCode))
                    .willSetStateTo("Attempt " + (i + 2)));
        }

        WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                .inScenario("RemoveTag_Retry")
                .whenScenarioStateIs("Attempt " + (maxAttempts - 1))
                .willReturn(WireMock.aResponse().withStatus(httpCode).withBody(""))
                .willSetStateTo(""));
    }

    public void setupStubForRetry_LastSuccessful(int httpCode) {
        int maxAttempts = retryDefaultProperties.getMaxAttempts();

        WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                .inScenario("RemoveTag_Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(WireMock.aResponse().withStatus(httpCode))
                .willSetStateTo("Attempt 1"));

        for (int i = 0; i < maxAttempts - 2; ++i) {
            WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                    .inScenario("RemoveTag_Retry")
                    .whenScenarioStateIs("Attempt " + (i + 1))
                    .willReturn(WireMock.aResponse().withStatus(httpCode))
                    .willSetStateTo("Attempt " + (i + 2)));
        }

        WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                .inScenario("RemoveTag_Retry")
                .whenScenarioStateIs("Attempt " + (maxAttempts - 1))
                .willReturn(WireMock.aResponse().withStatus(200).withBody("{\"id\": 1,\"url\":\"linkExample\"}"))
                .willSetStateTo(""));
    }

    public void verifyNumberOfCall(Integer numberOfCall) {
        WireMock.verify(numberOfCall, deleteRequestedFor(urlEqualTo("/removetag")));
    }

    @Autowired
    private SimpleClientHttpRequestFactory requestFactory;

    @Autowired
    private CircuitBreakerDefaultProperties circuitBreakerProperties;

    @Test
    public void handle_WhenTheNumberOfFailAttemptsExceededTheLimit_ThenReturnErrorSendMessage()
            throws InterruptedException {
        int numberOfCalls = circuitBreakerProperties.getMinimumNumberOfCalls();
        int timeout = properties.getTimeout().getConnectTimeout()
                + properties.getTimeout().getReadTimeout();
        WireMock.stubFor(WireMock.delete(urlEqualTo("/removetag"))
                .willReturn(WireMock.aResponse().withStatus(200).withFixedDelay(timeout + 200)));
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("linkExample", List.of("tag"), null));
        String expectedMessage = "Ошибка. Сервис недоступен, попробуйте позже :(";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/removetag tagExample");
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .requestFactory(requestFactory)
                .build();

        for (int i = 0; i < numberOfCalls; ++i) {
            SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);
            assertNotEquals(expectedMessage, actualMessage.getParameters().get("text"));
        }

        SendMessage actualMessage = removeTagMessageHandler.handle(update, restClient);
        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
    }
}
