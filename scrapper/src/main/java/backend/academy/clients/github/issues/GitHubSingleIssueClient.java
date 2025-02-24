package backend.academy.clients.github.issues;

import backend.academy.clients.Client;
import backend.academy.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GitHubSingleIssueClient implements Client {
    private static final Pattern SUPPORTED_URL = Pattern.compile("^https://github.com/(\\w+)/(\\w+)/issues/(\\d+)$");

    @Override
    public boolean supportLink(Link link) {
        String url = link.getUrl();
        Matcher linkMatcher = SUPPORTED_URL.matcher(url);
        return linkMatcher.matches();
    }

    @Override
    public List<String> getUpdates(Link link, RestClient client) {
        ObjectMapper objectMapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();
        String url = getUrl(link);
        if (url == null) return new ArrayList<>();

        GitHubIssue issuesList = client.method(HttpMethod.GET)
                .uri(url)
                .header("Accept", "application/vnd.github+json")
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return objectMapper.readValue(response.getBody(), GitHubIssue.class);
                    }
                    throw new RuntimeException(String.format(
                            "Не удалось получить обновления по ссылке: %s (%d)",
                            link.getUrl(), response.getStatusCode().value()));
                });

        return createUpdatesList(issuesList, link);
    }

    private String getUrl(Link link) {
        Matcher matcher = SUPPORTED_URL.matcher(link.getUrl());
        if (matcher.matches()) {
            return String.format(
                    "https://api.github.com/repos/%s/%s/issues/%s",
                    matcher.group(1), matcher.group(2), matcher.group(3));
        }
        return null;
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
