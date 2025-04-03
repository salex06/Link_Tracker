package backend.academy.scheduler;

import backend.academy.ScrapperConfig;
import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.dto.LinkUpdate;
import backend.academy.model.plain.Link;
import backend.academy.notifications.NotificationSender;
import backend.academy.service.LinkService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Scheduler {
    private final LinkService linkService;
    private final ClientManager clientManager;
    private final NotificationSender notificationSender;
    private final ScrapperConfig scrapperConfig;

    @Autowired
    public Scheduler(
            LinkService linkService, ClientManager clientManager, NotificationSender sender, ScrapperConfig config) {
        this.linkService = linkService;
        this.clientManager = clientManager;
        this.notificationSender = sender;
        this.scrapperConfig = config;
    }

    @Scheduled(fixedDelay = 50000, initialDelay = 10000)
    public void schedule() {
        int pageNumber = 0;
        int pageSize = scrapperConfig.pageSize().intValue();
        Page<Link> page;

        List<Client> clients = clientManager.getAvailableClients();

        int partCount = 4;
        List<List<Link>> dividedBatch = IntStream.range(0, partCount)
                .mapToObj(i -> new ArrayList<Link>())
                .collect(Collectors.toList());

        ExecutorService executorService = Executors.newFixedThreadPool(partCount);
        List<Future<?>> futures = new ArrayList<>();
        try {
            do {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                page = linkService.getAllLinks(pageable, Duration.of(10, ChronoUnit.SECONDS));
                divideBatch(page.getContent(), dividedBatch);

                for (List<Link> part : dividedBatch) {
                    List<Link> partCopy = new ArrayList<>(part);
                    Future<?> future = executorService.submit(() -> processLinks(clients, partCopy));
                    futures.add(future);
                }

                dividedBatch.forEach(List::clear);
                pageNumber++;
            } while (page.hasNext());
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Прервано ожидание завершения потоков");
                Thread.currentThread().interrupt();
            }
        }
        futures.forEach(i -> {
            try {
                i.get();
            } catch (RuntimeException | InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void divideBatch(List<Link> content, List<List<Link>> destination) {
        int partSize = (content.size() + 3) / 4;
        for (int i = 0; i < content.size(); ++i) {
            Link current = content.get(i);
            destination.get(i / partSize).add(current);
        }
    }

    private void processLinks(List<Client> clients, List<Link> links) {
        for (Link link : links) {
            processLink(clients, link);
        }
    }

    private void processLink(List<Client> clients, Link link) {
        Client suitableClient = getSuitableClient(clients, link);
        List<String> updateDescription = suitableClient.getUpdates(link);
        if (!updateDescription.isEmpty()) {
            linkService.updateLastUpdateTime(link, Instant.now());
            sendUpdates(updateDescription, link);
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
