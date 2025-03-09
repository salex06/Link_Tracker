package backend.academy.scheduler;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.exceptions.ApiErrorException;
import backend.academy.model.Link;
import backend.academy.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class Scheduler {
    private final LinkService linkService;
    private final ClientManager clientManager;
    private final RestClient botUpdatesClient;

    @Autowired
    public Scheduler(LinkService linkService, ClientManager clientManager, RestClient botUpdatesClient) {
        this.linkService = linkService;
        this.clientManager = clientManager;
        this.botUpdatesClient = botUpdatesClient;
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
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

            try {
                log.atInfo()
                        .setMessage("Отправка уведомления об обновлении")
                        .addKeyValue("url", linkUpdate.url())
                        .addKeyValue("description", linkUpdate.description())
                        .addKeyValue("tg-chat-ids", linkUpdate.tgChatIds())
                        .log();

                botUpdatesClient.post().uri("/updates").body(linkUpdate).exchange((request, response) -> {
                    if (response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                        ApiErrorResponse apiErrorResponse =
                                new ObjectMapper().readValue(response.getBody(), ApiErrorResponse.class);
                        throw new ApiErrorException(apiErrorResponse);
                    }
                    return null;
                });
            } catch (ApiErrorException e) {
                ApiErrorResponse response = e.apiErrorResponse();

                log.atError()
                        .setMessage("Некорректные параметры запроса")
                        .addKeyValue("description", response.description())
                        .addKeyValue("code", response.code())
                        .addKeyValue("exception-name", response.exceptionName())
                        .addKeyValue("exception-message", response.exceptionName())
                        .log();
            }
        }
    }
}
