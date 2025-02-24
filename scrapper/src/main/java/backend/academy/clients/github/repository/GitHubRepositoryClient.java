package backend.academy.clients.github.repository;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubRepositoryClient extends Client {
    private static final Pattern supportedUrl = Pattern.compile("^https://github.com/(\\w+)/(\\w+)$");

    public GitHubRepositoryClient(@Qualifier("gitHubRepositoryConverter") LinkToApiLinkConverter converter) {
        super(supportedUrl, converter);
    }

    @Override
    public List<String> getUpdates(Link link, RestClient client) {
        String url = linkConverter.convert(link.getUrl());
        if (url == null) {
            return null;
        }

        ResponseEntity<GitHubRepositoryDTO> response = client.method(HttpMethod.GET)
                .uri(url)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .toEntity(GitHubRepositoryDTO.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return List.of(generateUpdateText(Objects.requireNonNull(response.getBody()), link));
        } else {
            throw new RuntimeException(String.format(
                    "Не удалось получить обновления по ссылке: %s (%d)",
                    link.getUrl(), response.getStatusCode().value()));
        }
    }

    private String generateUpdateText(GitHubRepositoryDTO body, Link link) {
        String updateDescription = "";
        LocalDateTime previousUpdateTime = link.getLastUpdateTime();
        if (wasUpdated(previousUpdateTime, body.updatedAt())) {
            link.setLastUpdateTime(body.updatedAt());
            updateDescription =
                    String.format("Обновление репозитория %s по ссылке %s", body.repositoryName(), body.linkValue());
        }
        return updateDescription;
    }

    private boolean wasUpdated(LocalDateTime previousUpdateTime, LocalDateTime currentUpdateTime) {
        return previousUpdateTime == null || previousUpdateTime.isBefore(currentUpdateTime);
    }
}
