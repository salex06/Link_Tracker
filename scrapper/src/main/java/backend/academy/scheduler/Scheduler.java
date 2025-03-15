package backend.academy.scheduler;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.LinkUpdate;
import backend.academy.model.plain.Link;
import backend.academy.notifications.NotificationSender;
import backend.academy.service.LinkService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Scheduler {
    private final LinkService linkService;
    private final ClientManager clientManager;
    private final NotificationSender notificationSender;

    @Autowired
    public Scheduler(LinkService linkService, ClientManager clientManager, NotificationSender sender) {
        this.linkService = linkService;
        this.clientManager = clientManager;
        this.notificationSender = sender;
    }

    @Scheduled(fixedDelay = 50000, initialDelay = 10000)
    public void schedule() {
        List<Client> clients = clientManager.availableClients();
        List<Link> links = linkService.getAllLinks();
        for (Link link : links) {
            Client suitableClient = getSuitableClient(clients, link);
            List<String> updateDescription = suitableClient.getUpdates(link);
            if (!updateDescription.isEmpty()) {
                sendUpdates(updateDescription, link);
            }
        }
    }

    private Client getSuitableClient(List<Client> clients, Link link) {
        for (Client client : clients) {
            if (client.supportLink(link.getUrl())) {
                return client;
            }
        }

        throw new RuntimeException("No suitable clients for link: " + link.getUrl());
    }

    private void sendUpdates(List<String> updatesList, Link link) {
        for (String updateDescription : updatesList) {
            if (updateDescription.isEmpty()) {
                continue;
            }

            LinkUpdate linkUpdate = new LinkUpdate(
                    link.getId(),
                    link.getUrl(),
                    updateDescription,
                    link.getTgChatIds().stream().toList());

            notificationSender.send(linkUpdate);
        }
    }
}
