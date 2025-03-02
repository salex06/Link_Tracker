package backend.academy.scheduler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.model.Link;
import backend.academy.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

class SchedulerTest {
    private final int port = 8089;
    private WireMockServer wireMockServer;

    private Scheduler scheduler;

    private LinkService linkService;
    private ClientManager clientManager;
    private RestClient restClient;

    @BeforeEach
    public void setUp() {
        linkService = Mockito.mock(LinkService.class);
        clientManager = Mockito.mock(ClientManager.class);
        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();

        scheduler = new Scheduler(linkService, clientManager, restClient);
    }

    @BeforeEach
    public void setupWireMock() {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
    }

    @AfterEach
    public void shutdown() {
        wireMockServer.stop();
    }

    @Test
    public void schedule_WhenAnyCorrectUpdate_ThenBotReturnsOK() {
        String expectedResponse = "123";
        List<String> updates = List.of("Новое обновление от пользователя X по ссылке Y");
        Client mockedClient = Mockito.mock(Client.class);
        when(mockedClient.supportLink(any(String.class))).thenReturn(true);
        when(mockedClient.getUpdates(any(Link.class))).thenReturn(updates);
        List<Client> availableClients = List.of(mockedClient);
        List<Link> links = List.of(new Link(1L, "testLink"));
        when(clientManager.availableClients()).thenReturn(availableClients);
        when(linkService.getAllLinks()).thenReturn(links);

        stubFor(post("/updates")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withHeader("charset", "utf-8")
                        .withBody("123")));

        scheduler.schedule();

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
            assertEquals(
                    expectedResponse,
                    wireMockServer.getAllServeEvents().getFirst().getResponse().getBodyAsString());
        });
    }

    @Test
    public void schedule_WhenAnyWrongUpdate_ThenBotReturnBadRequest() {
        ApiErrorResponse expectedResponse =
                new ApiErrorResponse("Некорректные параметры запроса", "400", null, null, null);
        List<String> updates = List.of("Новое обновление от пользователя X по ссылке Y");
        Client mockedClient = Mockito.mock(Client.class);
        when(mockedClient.supportLink(any(String.class))).thenReturn(true);
        when(mockedClient.getUpdates(any(Link.class))).thenReturn(updates);
        List<Client> availableClients = List.of(mockedClient);
        List<Link> links = List.of(new Link(1L, "testLink"));
        when(clientManager.availableClients()).thenReturn(availableClients);
        when(linkService.getAllLinks()).thenReturn(links);

        stubFor(post("/updates")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "text/plain")
                        .withHeader("charset", "utf-8")
                        .withBody("{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
                                + "\"exceptionName\":null, \"exceptionMessage\": null, \"stacktrace\": null}")));

        scheduler.schedule();

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
            assertEquals(
                    400,
                    wireMockServer.getAllServeEvents().getFirst().getResponse().getStatus());
            String response =
                    wireMockServer.getAllServeEvents().getFirst().getResponse().getBodyAsString();
            ApiErrorResponse apiErrorResponse = new ObjectMapper().readValue(response, ApiErrorResponse.class);
            assertEquals(expectedResponse, apiErrorResponse);
        });
    }
}
