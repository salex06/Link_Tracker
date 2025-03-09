package backend.academy.clients.github.issues;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Класс для отслеживания изменений в комментариях к issue или pull request на GitHub */
@Component
public class GitHubIssueListClient extends Client {
    private static final Pattern SUPPORTED_URL = Pattern.compile("^https://github.com/(\\w+)/(\\w+)/(issues|pulls)$");

    public GitHubIssueListClient(
            @Qualifier("gitHubIssueListClientConverter") LinkToApiLinkConverter linkConverter,
            @Qualifier("gitHubClient") RestClient restClient) {
        super(SUPPORTED_URL, linkConverter, restClient);
    }

    @Override
    public List<String> getUpdates(Link link) {
        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();
        String url = linkConverter.convert(link.getUrl());
        if (url == null) {
            return List.of();
        }

        List<GitHubComment> data = getComments(objectMapper, url);

        if (data == null || data.isEmpty()) {
            return List.of();
        }

        return createListOfUpdates(objectMapper, link, data);
    }

    private List<GitHubComment> getComments(ObjectMapper mapper, String url) {
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readValue(response.getBody(), new TypeReference<>() {});
            }
            return null;
        });
    }

    private List<String> createListOfUpdates(ObjectMapper objectMapper, Link link, List<GitHubComment> comments) {
        List<String> updates = new ArrayList<>();
        LocalDateTime latestUpdateTime = link.getLastUpdateTime();

        for (GitHubComment comment : comments) {
            if (issueWasUpdated(link.getLastUpdateTime(), comment.createdAt())) {
                GitHubIssue issue = getIssue(objectMapper, comment.issueUrl());
                updates.add(createUpdate(comment, issue));
                latestUpdateTime =
                        (latestUpdateTime.isBefore(comment.createdAt()) ? comment.createdAt() : latestUpdateTime);
            }
        }

        link.setLastUpdateTime(latestUpdateTime);
        return updates;
    }

    private GitHubIssue getIssue(ObjectMapper mapper, String issueUrl) {
        return client.get().uri(issueUrl).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readValue(response.getBody(), GitHubIssue.class);
            }
            return null;
        });
    }

    private boolean issueWasUpdated(LocalDateTime lastUpdateTime, LocalDateTime commentCreateDateTime) {
        return lastUpdateTime.isBefore(commentCreateDateTime);
    }

    private String createUpdate(GitHubComment comment, GitHubIssue issue) {
        return String.format(
                "Новый комментарий к issue %s%nАвтор: %s%nВремя создания: %s%nПревью: %s",
                issue.title(), comment.user().ownerName(), comment.createdAt(), comment.body());
    }
}
