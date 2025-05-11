package backend.academy.clients.github.storage;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.LinkUpdateInfo;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
public class GitHubPersonalStorageClient extends Client {
    private static final Pattern supportedUrl =
            Pattern.compile("^https://github\\.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)$");

    private final ApplicationStabilityProperties stabilityProperties;

    public GitHubPersonalStorageClient(
            @Qualifier("gitHubRepositoryConverter") LinkToApiLinkConverter converter,
            @Qualifier("gitHubClient") RestClient gitHubClient,
            ApplicationStabilityProperties stabilityProperties) {
        super(supportedUrl, converter, gitHubClient);
        this.stabilityProperties = stabilityProperties;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "onError")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBError")
    public List<LinkUpdateInfo> getUpdates(Link link) {
        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();

        String url = linkConverter.convert(link.getUrl());
        if (url == null) {
            return List.of();
        }

        log.atInfo()
                .setMessage("Запрос к GitHub Api (репозиторий)")
                .addKeyValue("url", url)
                .log();

        GitHubRepositoryDTO data = client.method(HttpMethod.GET)
                .uri(url)
                .header("Accept", "application/vnd.github+json")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return objectMapper.readValue(response.getBody(), GitHubRepositoryDTO.class);
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

        if (data != null) {
            return generateUpdateText(data, link);
        } else {
            log.atError()
                    .setMessage("Ошибка при обращении к GitHub Api")
                    .addKeyValue("url", url)
                    .log();

            return List.of();
        }
    }

    private List<LinkUpdateInfo> generateUpdateText(GitHubRepositoryDTO body, Link link) {
        List<LinkUpdateInfo> updateDescription = new ArrayList<>();
        Instant previousUpdateTime = link.getLastUpdateTime();
        if (wasUpdated(previousUpdateTime, body.updatedAt())) {
            updateDescription.add(new LinkUpdateInfo(
                    body.linkValue(),
                    body.owner().ownerName(),
                    body.repositoryName(),
                    null,
                    body.updatedAt(),
                    String.format("Обновление репозитория %s по ссылке %s", body.repositoryName(), body.linkValue())));
        }

        return updateDescription;
    }

    private boolean wasUpdated(Instant previousUpdateTime, Instant currentUpdateTime) {
        return previousUpdateTime == null || previousUpdateTime.isBefore(currentUpdateTime);
    }

    public List<LinkUpdateInfo> onError(Link link, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении списка обновлений репозитория. Неудачный запрос")
                .addKeyValue("url", link.getUrl())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return List.of();
    }

    public List<LinkUpdateInfo> onCBError(Link link, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении списка обновлений репозитория. Сервис недоступен")
                .addKeyValue("url", link.getUrl())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return List.of();
    }
}
