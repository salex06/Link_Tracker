package backend.academy.clients.github.storage;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubPersonalStorageClient extends Client {
    private static final Pattern supportedUrl =
            Pattern.compile("^https://github\\.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)$");

    public GitHubPersonalStorageClient(
            @Qualifier("gitHubRepositoryConverter") LinkToApiLinkConverter converter,
            @Qualifier("gitHubClient") RestClient gitHubClient) {
        super(supportedUrl, converter, gitHubClient);
    }

    @Override
    public List<String> getUpdates(Link link) {
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
                    }

                    log.atError()
                            .setMessage("Некорректные параметры запроса к GitHub API")
                            .addKeyValue("url", url)
                            .log();

                    return null;
                });

        if (data != null) {
            return List.of(generateUpdateText(data, link));
        } else {
            log.atError()
                    .setMessage("Ошибка при обращении к GitHub Api")
                    .addKeyValue("url", url)
                    .log();

            return List.of();
        }
    }

    private String generateUpdateText(GitHubRepositoryDTO body, Link link) {
        String updateDescription = "";
        Instant previousUpdateTime = link.getLastUpdateTime();
        if (wasUpdated(previousUpdateTime, body.updatedAt())) {
            // link.setLastUpdateTime(body.updatedAt());
            updateDescription =
                    String.format("Обновление репозитория %s по ссылке %s", body.repositoryName(), body.linkValue());
        }

        return updateDescription;
    }

    private boolean wasUpdated(Instant previousUpdateTime, Instant currentUpdateTime) {
        return previousUpdateTime == null || previousUpdateTime.isBefore(currentUpdateTime);
    }
}
