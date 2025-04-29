package backend.academy.clients.github.issues;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.dto.LinkUpdateInfo;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubSingleIssueClient extends Client {
    private static final Pattern SUPPORTED_URL =
            Pattern.compile("^https://github.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/issues/(\\d+)$");

    private final RetryTemplate retryTemplate;

    public GitHubSingleIssueClient(
            @Qualifier("gitHubSingleIssueConverter") LinkToApiLinkConverter converter,
            @Qualifier("gitHubClient") RestClient gitHubRestClient,
            RetryTemplate retryTemplate) {
        super(SUPPORTED_URL, converter, gitHubRestClient);
        this.retryTemplate = retryTemplate;
    }

    @Override
    public List<LinkUpdateInfo> getUpdates(Link link) {
        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();

        String url = linkConverter.convert(link.getUrl());
        if (url == null) return new ArrayList<>();

        log.atInfo()
                .setMessage("Обращение к GitHub API")
                .addKeyValue("url", url)
                .log();

        GitHubIssue issuesList = retryTemplate.execute(
                context -> client.method(HttpMethod.GET)
                        .uri(url)
                        .header("Accept", "application/vnd.github+json")
                        .exchange((request, response) -> {
                            if (response.getStatusCode().is2xxSuccessful()) {
                                return objectMapper.readValue(response.getBody(), GitHubIssue.class);
                            } else if (response.getStatusCode().isError()) {
                                throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                            }

                            log.atError()
                                    .setMessage("Некорректные параметры запроса к GitHub API")
                                    .addKeyValue("url", url)
                                    .log();

                            return null;
                        }),
                context -> null);

        link.setLastUpdateTime(Instant.now());
        return createUpdatesList(issuesList, link);
    }

    private List<LinkUpdateInfo> createUpdatesList(GitHubIssue gitHubIssue, Link link) {
        if (gitHubIssue == null) {
            return List.of();
        }

        List<LinkUpdateInfo> updatesList = new ArrayList<>();
        Instant previousUpdateTime = link.getLastUpdateTime();

        if (wasUpdated(previousUpdateTime, gitHubIssue.updatedAt())) {
            updatesList.add(new LinkUpdateInfo(
                    gitHubIssue.linkValue(),
                    gitHubIssue.author().ownerName(),
                    gitHubIssue.title(),
                    gitHubIssue.description(),
                    gitHubIssue.updatedAt(),
                    String.format("Обновление issue #%s по ссылке %s", gitHubIssue.title(), gitHubIssue.linkValue())));
        }

        return updatesList;
    }

    private boolean wasUpdated(Instant previousUpdateTime, Instant currentUpdateTime) {
        return previousUpdateTime == null || previousUpdateTime.isBefore(currentUpdateTime);
    }
}
