package backend.academy.clients.github.issues;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
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
public class GitHubSingleIssueClient extends Client {
    private static final Pattern SUPPORTED_URL = Pattern.compile("^https://github.com/(\\w+)/(\\w+)/issues/(\\d+)$");

    public GitHubSingleIssueClient(
            @Qualifier("gitHubSingleIssueConverter") LinkToApiLinkConverter converter,
            @Qualifier("gitHubClient") RestClient gitHubRestClient) {
        super(SUPPORTED_URL, converter, gitHubRestClient);
    }

    @Override
    public List<String> getUpdates(Link link) {
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
                    }

                    log.atError()
                            .setMessage("Некорректные параметры запроса к GitHub API")
                            .addKeyValue("url", url)
                            .log();

                    return null;
                });

        return createUpdatesList(issuesList, link);
    }

    private List<String> createUpdatesList(GitHubIssue gitHubIssue, Link link) {
        if (gitHubIssue == null) {
            return List.of();
        }

        List<String> updatesList = new ArrayList<>();
        LocalDateTime previousUpdateTime = link.getLastUpdateTime();

        if (wasUpdated(previousUpdateTime, gitHubIssue.updatedAt())) {
            updatesList.add(
                    String.format("Обновление issue #%s по ссылке %s", gitHubIssue.title(), gitHubIssue.linkValue()));
            link.setLastUpdateTime(gitHubIssue.updatedAt());
        }

        return updatesList;
    }

    private boolean wasUpdated(LocalDateTime previousUpdateTime, LocalDateTime currentUpdateTime) {
        return previousUpdateTime == null || previousUpdateTime.isBefore(currentUpdateTime);
    }
}
