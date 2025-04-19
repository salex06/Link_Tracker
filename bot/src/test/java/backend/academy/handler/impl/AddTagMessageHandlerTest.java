package backend.academy.handler.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.commands.Command;
import backend.academy.crawler.impl.tags.add.AddTagMessageCrawler;
import backend.academy.dto.AddLinkRequest;
import backend.academy.service.RedisCacheService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

class AddTagMessageHandlerTest {
    private int port;

    @Autowired
    private static RestClient restClient;

    private AddTagMessageCrawler crawler;

    private AddTagMessageHandler addTagMessageHandler;

    private WireMockServer wireMockServer;

    private RedisCacheService mockedRedisService;

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
        crawler = Mockito.mock(AddTagMessageCrawler.class);
        mockedRedisService = Mockito.mock(RedisCacheService.class);
        addTagMessageHandler = new AddTagMessageHandler(crawler, mockedRedisService);
    }

    @Test
    public void supportCommand_WhenCorrectCommand_ThenReturnTrue() {
        Command command = new Command("/addtag", true);

        boolean result = addTagMessageHandler.supportCommand(command);

        assertThat(result).isTrue();
    }

    @Test
    public void supportCommand_WhenWrongCommand_ThenReturnFalse() {
        Command command = new Command("/anyCommand", false);

        boolean result = addTagMessageHandler.supportCommand(command);

        assertThat(result).isFalse();
    }

    @Test
    public void handle_WhenHeaderWasNotPassed_ThenReturnError() {
        crawler = Mockito.mock(AddTagMessageCrawler.class);
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("link", List.of("tag"), new ArrayList<>()));
        addTagMessageHandler = new AddTagMessageHandler(crawler, mockedRedisService);
        String expectedMessage = "Некорректные параметры запроса";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(5L);
        when(message.text()).thenReturn("/addtag tagExample");
        stubFor(
                post("/addtag")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"stacktrace\": []}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = addTagMessageHandler.handle(update, restClient);

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
        when(message.text()).thenReturn("/addtag tagExample");
        stubFor(
                post("/addtag")
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                                        + "\"exceptionName\":\"MissingRequestHeaderException\", \"exceptionMessage\": \"Required request header 'Tg-Chat-Id' for method parameter type Long is not present\", \"stacktrace\": []}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = addTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
    }

    @Test
    public void handle_WhenCorrectRequestAndAddToAll_ThenReturnLinkResponse() {
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("linkExample", List.of("tag"), null));
        String expectedMessage = "Тег успешно добавлен!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/addtag tagExample");
        stubFor(post("/addtag")
                .willReturn(aResponse()
                        .withHeader("Tg-Chat-Id", chat.id().toString())
                        .withHeader("Add-To-All", "true")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": null,\"url\": null, \"tags\" : [\"tagExample\"], \"filters\":[]}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = addTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
    }

    @Test
    public void handle_WhenCorrectRequest_ThenReturnLinkResponse() {
        when(crawler.terminate(anyLong())).thenReturn(new AddLinkRequest("link.link", List.of("tag"), null));
        String expectedMessage = "Тег успешно добавлен!";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(50L);
        when(message.text()).thenReturn("/addtag tagExample");
        stubFor(post("/addtag")
                .willReturn(aResponse()
                        .withHeader("Tg-Chat-Id", chat.id().toString())
                        .withHeader("Add-To-All", "false")
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1,\"url\":\"linkExample\"}")));
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        SendMessage actualMessage = addTagMessageHandler.handle(update, restClient);

        assertEquals(expectedMessage, actualMessage.getParameters().get("text"));
        verify(mockedRedisService, times(1)).invalidateCache();
    }
}
