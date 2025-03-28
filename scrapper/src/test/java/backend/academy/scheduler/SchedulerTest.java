package backend.academy.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.ScrapperConfig;
import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.LinkUpdate;
import backend.academy.model.plain.Link;
import backend.academy.notifications.NotificationSender;
import backend.academy.notifications.impl.HttpNotificationSender;
import backend.academy.service.LinkService;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class SchedulerTest {
    private static final String CLIENT1_SUPPORTED_URL = "client1";
    private static final Long CLIENT1_NO_UPDATES_INDICATOR = 1L;
    private static final Long CLIENT2_NO_UPDATES_INDICATOR = 2L;
    private static final String CLIENT2_SUPPORTED_URL = "client2";
    private static Scheduler scheduler;

    private static LinkService linkService;
    private static ClientManager clientManager;
    private static NotificationSender notificationSender;
    private static ScrapperConfig scrapperConfig;
    private static List<Client> clients;
    private static Client client1;
    private static Client client2;

    @BeforeAll
    public static void setUp() {
        linkService = Mockito.mock(LinkService.class);
        clientManager = Mockito.mock(ClientManager.class);
        notificationSender = Mockito.mock(HttpNotificationSender.class);
        scrapperConfig = Mockito.mock(ScrapperConfig.class);

        setUpClients();

        when(scrapperConfig.pageSize()).thenReturn(50L);
        when(clientManager.availableClients()).thenReturn(clients);

        scheduler = new Scheduler(linkService, clientManager, notificationSender, scrapperConfig);
    }

    private static void setUpClients() {
        client1 = Mockito.mock(Client.class);
        when(client1.supportLink(anyString())).thenAnswer(invocationOnMock -> {
            String url = invocationOnMock.getArgument(0);
            return Objects.equals(url, CLIENT1_SUPPORTED_URL);
        });
        when(client1.getUpdates(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            if (Objects.equals(link.getId(), CLIENT1_NO_UPDATES_INDICATOR)) {
                return List.of();
            }
            return List.of("update1", "update2");
        });

        client2 = Mockito.mock(Client.class);
        when(client2.supportLink(anyString())).thenAnswer(invocationOnMock -> {
            String url = invocationOnMock.getArgument(0);
            return Objects.equals(url, CLIENT2_SUPPORTED_URL);
        });
        when(client2.getUpdates(any(Link.class))).thenAnswer(invocationOnMock -> {
            Link link = invocationOnMock.getArgument(0);
            if (Objects.equals(link.getId(), CLIENT2_NO_UPDATES_INDICATOR)) {
                return List.of();
            }
            return List.of("update3", "update4");
        });

        clients = List.of(client1, client2);
    }

    @Test
    public void schedule_WhenNoSuitableClients_ThenThrowException() {
        Link link1 = new Link(1L, "wrong_url", List.of("tag"), List.of("filter"), Set.of(1L));
        when(linkService.getAllLinks(any(Pageable.class), any(Duration.class)))
                .thenReturn(new PageImpl<>(List.of(link1)));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> scheduler.schedule());
        assertEquals("No suitable clients for link: " + link1.getUrl(), ex.getMessage());
    }

    @Test
    public void schedule_WhenNoUpdates_ThenDoNothing() {
        Link link1 = new Link(
                CLIENT2_NO_UPDATES_INDICATOR, CLIENT2_SUPPORTED_URL, List.of("tag"), List.of("filter"), Set.of(1L));

        when(linkService.getAllLinks(any(Pageable.class), any(Duration.class)))
                .thenReturn(new PageImpl<>(List.of(link1)));

        scheduler.schedule();

        verify(notificationSender, times(0)).send(any(LinkUpdate.class));
    }

    @Test
    public void schedule_WhenUpdatesExist_ThenSendUpdates() {
        List<String> linkUpdate1 = List.of("update1", "update2");
        List<String> linkUpdate2 = List.of("update3", "update4");
        Link link1 = new Link(5L, CLIENT1_SUPPORTED_URL, List.of("tag"), List.of("filter"), Set.of(1L));
        Link link2 = new Link(3L, CLIENT2_SUPPORTED_URL, List.of("tag"), List.of("filter"), Set.of(1L));
        when(linkService.getAllLinks(any(Pageable.class), any(Duration.class)))
                .thenReturn(new PageImpl<>(List.of(link1, link2)));

        scheduler.schedule();

        verify(notificationSender, times(1)).send(new LinkUpdate(5L, CLIENT1_SUPPORTED_URL, "update1", List.of(1L)));
        verify(notificationSender, times(1)).send(new LinkUpdate(5L, CLIENT1_SUPPORTED_URL, "update2", List.of(1L)));
        verify(notificationSender, times(1)).send(new LinkUpdate(3L, CLIENT2_SUPPORTED_URL, "update3", List.of(1L)));
        verify(notificationSender, times(1)).send(new LinkUpdate(3L, CLIENT2_SUPPORTED_URL, "update4", List.of(1L)));
    }
}
