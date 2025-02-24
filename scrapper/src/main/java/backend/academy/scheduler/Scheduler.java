package backend.academy.scheduler;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.LinkUpdate;
import backend.academy.model.Link;
import backend.academy.service.LinkService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class Scheduler {
    private final LinkService linkService;
    private final ClientManager clientManager;
    private final RestClient restClient;

    @Autowired
    public Scheduler(LinkService linkService, ClientManager clientManager, RestClient client) {
        this.linkService = linkService;
        this.clientManager = clientManager;
        this.restClient = client;
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void schedule() {
        List<Client> clients = clientManager.availableClients();
        List<Link> links = linkService.getAllLinks();
        for (Link link : links) {
            Client suitableClient = getSuitableClient(clients, link);
            List<String> updateDescription = suitableClient.getUpdates(link, restClient);
            if (!updateDescription.isEmpty()) {
                sendUpdates(updateDescription, link);
            }
        }
    }

    private Client getSuitableClient(List<Client> clients, Link link) {
        for (Client client : clients) {
            if (client.supportLink(link)) {
                return client;
            }
        }
        throw new RuntimeException("No suitable clients for link: " + link.getUrl());
    }

    private void sendUpdates(List<String> updatesList, Link link) {
        // TODO: централизовать логику отправления сообщений
        for (String updateDescription : updatesList) {
            if (updateDescription.isEmpty()) {
                continue;
            }
            LinkUpdate linkUpdate = new LinkUpdate(link.getId(), link.getUrl(), updateDescription, link.getTgChatIds());
            String url = "http://localhost:8080/updates";
            RestClient client = RestClient.create();

            client.post().uri(url).body(linkUpdate).retrieve().toEntity(String.class);
        }
    }
}
