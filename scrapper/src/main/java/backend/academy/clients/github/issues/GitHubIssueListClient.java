package backend.academy.clients.github.issues;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Класс для отслеживания изменений в комментариях к issue или pull request на GitHub */
@Slf4j
@Component
public class GitHubIssueListClient extends Client {
    private static final Pattern SUPPORTED_URL =
            Pattern.compile("^https://github.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/(issues|pulls)$");

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

        List<GitHubIssue> issues = getIssues(objectMapper, url);
        if (issues == null || issues.isEmpty()) {
            return List.of();
        }
        List<String> newIssues = createListOfNewIssue(issues, link);

        List<List<GitHubComment>> commentsForEachIssue = getCommentsForEachIssue(objectMapper, issues);
        List<String> newComments = new ArrayList<>();
        for (List<GitHubComment> commentList : commentsForEachIssue) {
            if (commentList == null || commentList.isEmpty()) {
                continue;
            }
            newComments.addAll(createListOfCommentUpdates(objectMapper, link, commentList));
        }

        List<String> resultList = new ArrayList<>();
        resultList.addAll(newIssues);
        resultList.addAll(newComments);
        return resultList;
    }

    private List<GitHubIssue> getIssues(ObjectMapper objectMapper, String url) {
        log.atInfo()
                .setMessage("Обращение к GitHub API для получения issues/pull requests")
                .addKeyValue("url", url)
                .log();
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            }
            log.atWarn()
                    .setMessage("Неудачный запрос на получение issues/pull requests")
                    .addKeyValue("url", url)
                    .addKeyValue("code", response.getStatusCode())
                    .log();
            return null;
        });
    }

    private List<List<GitHubComment>> getCommentsForEachIssue(ObjectMapper objectMapper, List<GitHubIssue> issues) {
        List<List<GitHubComment>> commentsMatrix = new ArrayList<>();
        for (GitHubIssue issue : issues) {
            List<GitHubComment> commentsSet = getComments(objectMapper, issue.commentsUrl());
            commentsMatrix.add(commentsSet);
        }
        return commentsMatrix;
    }

    private List<GitHubComment> getComments(ObjectMapper mapper, String url) {
        log.atInfo()
                .setMessage("Обращение к GitHub API для получения комментариев")
                .addKeyValue("url", url)
                .log();
        return client.get().uri(url).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readValue(response.getBody(), new TypeReference<>() {});
            }
            log.atWarn()
                    .setMessage("Неудачный запрос на получение комментариев")
                    .addKeyValue("url", url)
                    .addKeyValue("code", response.getStatusCode())
                    .log();
            return null;
        });
    }

    private List<String> createListOfCommentUpdates(
            ObjectMapper objectMapper, Link link, List<GitHubComment> comments) {
        List<String> updates = new ArrayList<>();

        for (GitHubComment comment : comments) {
            if (issueWasUpdated(link.getLastUpdateTime(), comment.createdAt())) {
                GitHubIssue issue = getIssue(objectMapper, comment.issueUrl());
                updates.add(createNewCommentUpdate(comment, issue));
            }
        }
        return updates;
    }

    private List<String> createListOfNewIssue(List<GitHubIssue> issues, Link link) {
        List<String> issueUpdates = new ArrayList<>();

        for (GitHubIssue issue : issues) {
            if (newIssue(link.getLastUpdateTime(), issue.createdAt())) {
                issueUpdates.add(createNewIssueUpdate(issue));
            }
        }
        return issueUpdates;
    }

    private boolean newIssue(Instant lastUpdateTime, Instant issueCreationDate) {
        return lastUpdateTime.isBefore(issueCreationDate);
    }

    private GitHubIssue getIssue(ObjectMapper mapper, String issueUrl) {
        log.atInfo()
                .setMessage("Обращение к GitHub API для получения issue")
                .addKeyValue("url", issueUrl)
                .log();
        return client.get().uri(issueUrl).exchange((request, response) -> {
            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readValue(response.getBody(), GitHubIssue.class);
            }
            log.atWarn()
                    .setMessage("Неудачный запрос на получение issue")
                    .addKeyValue("url", issueUrl)
                    .addKeyValue("code", response.getStatusCode())
                    .log();
            return null;
        });
    }

    private boolean issueWasUpdated(Instant lastUpdateTime, Instant commentCreateDateTime) {
        return lastUpdateTime.isBefore(commentCreateDateTime);
    }

    private String createNewCommentUpdate(GitHubComment comment, GitHubIssue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return String.format(
                "Новый комментарий к issue %s%nАвтор: %s%nВремя создания: %s (UTC)%nПревью: %s",
                issue.title(),
                comment.user().ownerName(),
                formatter.format(LocalDateTime.ofInstant(comment.createdAt(), ZoneId.of("UTC"))),
                comment.body());
    }

    private String createNewIssueUpdate(GitHubIssue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return String.format(
                "Новый issue %s%nАвтор: %s%nВремя создания: %s (UTC)%nПревью: %s",
                issue.title(),
                issue.author().ownerName(),
                formatter.format(LocalDateTime.ofInstant(issue.createdAt(), ZoneId.of("UTC"))),
                issue.description());
    }
}
