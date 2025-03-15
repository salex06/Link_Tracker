package backend.academy.scheduler;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    //    private final int port = 8089;
    //    private WireMockServer wireMockServer;
    //
    //    private Scheduler scheduler;
    //
    //    private LinkService linkService;
    //    private ClientManager clientManager;
    //    private RestClient restClient;
    //    private NotificationSender notificationSender;
    //
    //    @BeforeEach
    //    public void setUp() {
    //        linkService = Mockito.mock(LinkService.class);
    //        clientManager = Mockito.mock(ClientManager.class);
    //        restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
    //        notificationSender = Mockito.mock(HttpNotificationSender.class);
    //
    //        scheduler = new Scheduler(linkService, clientManager, restClient, notificationSender);
    //    }
    //
    //    @BeforeEach
    //    public void setupWireMock() {
    //        wireMockServer = new WireMockServer(options().port(port));
    //        wireMockServer.start();
    //        WireMock.configureFor("localhost", port);
    //    }
    //
    //    @AfterEach
    //    public void shutdown() {
    //        wireMockServer.stop();
    //    }
    //
    //    @Test
    //    public void schedule_WhenAnyCorrectUpdate_ThenBotReturnsOK() {
    //        String expectedResponse = "123";
    //        List<String> updates = List.of("Новое обновление от пользователя X по ссылке Y");
    //        Client mockedClient = Mockito.mock(Client.class);
    //        when(mockedClient.supportLink(any(String.class))).thenReturn(true);
    //        when(mockedClient.getUpdates(any(Link.class))).thenReturn(updates);
    //        List<Client> availableClients = List.of(mockedClient);
    //        List<Link> links = List.of(new Link(1L, "testLink"));
    //        when(clientManager.availableClients()).thenReturn(availableClients);
    //        when(linkService.getAllLinks()).thenReturn(links);
    //
    //        stubFor(post("/updates")
    //                .willReturn(aResponse()
    //                        .withStatus(200)
    //                        .withHeader("Content-Type", "text/plain")
    //                        .withHeader("charset", "utf-8")
    //                        .withBody("123")));
    //
    //        scheduler.schedule();
    //
    //        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
    //            wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
    //            assertEquals(
    //                    expectedResponse,
    //                    wireMockServer.getAllServeEvents().getFirst().getResponse().getBodyAsString());
    //        });
    //    }
    //
    //    @Test
    //    public void schedule_WhenAnyWrongUpdate_ThenBotReturnBadRequest() {
    //        ApiErrorResponse expectedResponse =
    //                new ApiErrorResponse("Некорректные параметры запроса", "400", null, null, null);
    //        List<String> updates = List.of("Новое обновление от пользователя X по ссылке Y");
    //        Client mockedClient = Mockito.mock(Client.class);
    //        when(mockedClient.supportLink(any(String.class))).thenReturn(true);
    //        when(mockedClient.getUpdates(any(Link.class))).thenReturn(updates);
    //        List<Client> availableClients = List.of(mockedClient);
    //        List<Link> links = List.of(new Link(1L, "testLink"));
    //        when(clientManager.availableClients()).thenReturn(availableClients);
    //        when(linkService.getAllLinks()).thenReturn(links);
    //
    //        stubFor(post("/updates")
    //                .willReturn(aResponse()
    //                        .withStatus(400)
    //                        .withHeader("Content-Type", "text/plain")
    //                        .withHeader("charset", "utf-8")
    //                        .withBody("{\"description\":\"Некорректные параметры запроса\",\"code\":\"400\", "
    //                                + "\"exceptionName\":null, \"exceptionMessage\": null, \"stacktrace\": null}")));
    //
    //        scheduler.schedule();
    //
    //        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
    //            wireMockServer.verify(postRequestedFor(urlEqualTo("/updates")));
    //            assertEquals(
    //                    400,
    //                    wireMockServer.getAllServeEvents().getFirst().getResponse().getStatus());
    //            String response =
    //                    wireMockServer.getAllServeEvents().getFirst().getResponse().getBodyAsString();
    //            ApiErrorResponse apiErrorResponse = new ObjectMapper().readValue(response, ApiErrorResponse.class);
    //            assertEquals(expectedResponse, apiErrorResponse);
    //        });
    //    }
}
