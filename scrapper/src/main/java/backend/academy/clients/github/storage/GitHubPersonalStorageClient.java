package backend.academy.clients.github.storage;

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
public class GitHubPersonalStorageClient extends Client {
    private static final Pattern supportedUrl =
            Pattern.compile("^https://github\\.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)$");

    private final RetryTemplate retryTemplate;

    public GitHubPersonalStorageClient(
            @Qualifier("gitHubRepositoryConverter") LinkToApiLinkConverter converter,
            @Qualifier("gitHubClient") RestClient gitHubClient,
            RetryTemplate retryTemplate) {
        super(supportedUrl, converter, gitHubClient);
        this.retryTemplate = retryTemplate;
    }

    @Override
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

        GitHubRepositoryDTO data = retryTemplate.execute(
                context -> client.method(HttpMethod.GET)
                        .uri(url)
                        .header("Accept", "application/vnd.github+json")
                        .exchange((request, response) -> {
                            if (response.getStatusCode().is2xxSuccessful()) {
                                return objectMapper.readValue(response.getBody(), GitHubRepositoryDTO.class);
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
}
