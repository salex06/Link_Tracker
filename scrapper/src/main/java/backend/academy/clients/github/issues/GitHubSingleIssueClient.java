package backend.academy.clients.github.issues;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.clients.github.GitHubClient;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.LinkUpdateInfo;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@SuppressWarnings("PMD")
public class GitHubSingleIssueClient extends GitHubClient {
    private static final Pattern SUPPORTED_URL =
            Pattern.compile("^https://github.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/issues/(\\d+)$");

    private final ApplicationStabilityProperties stabilityProperties;

    public GitHubSingleIssueClient(
            @Qualifier("gitHubSingleIssueConverter") LinkToApiLinkConverter converter,
            @Qualifier("gitHubClient") RestClient gitHubRestClient,
            ApplicationStabilityProperties stabilityProperties) {
        super(SUPPORTED_URL, converter, gitHubRestClient);
        this.stabilityProperties = stabilityProperties;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    public List<LinkUpdateInfo> getUpdates(Link link) {
        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();

        String url = linkConverter.convert(link.getUrl());
        if (url == null) return new ArrayList<>();

        log.atInfo()
                .setMessage("Обращение к GitHub API")
                .addKeyValue("url", url)
                .log();

        GitHubIssue issuesList = client.method(HttpMethod.GET)
                .uri(url)
                .header("Accept", "application/vnd.github+json")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return objectMapper.readValue(response.getBody(), GitHubIssue.class);
                    } else if (stabilityProperties
                            .getRetry()
                            .getHttpCodes()
                            .contains(response.getStatusCode().value())) {
                        throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                    }

                    log.atError()
                            .setMessage("Некорректные параметры запроса к GitHub API")
                            .addKeyValue("url", url)
                            .log();

                    return null;
                });

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

    public List<LinkUpdateInfo> onError(Link link, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении списка обновлений. Неудачный запрос")
                .addKeyValue("url", link.getUrl())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return List.of();
    }

    public List<LinkUpdateInfo> onCBError(Link link, Throwable t, CallNotPermittedException e) {
        log.atWarn()
                .setMessage("Ошибка при получении списка обновлений. Сервис недоступен")
                .addKeyValue("url", link.getUrl())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return List.of();
    }
}
