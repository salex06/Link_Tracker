package backend.academy.clients.github.issues;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.clients.github.GitHubClient;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.dto.LinkUpdateInfo;
import backend.academy.exceptions.RetryableHttpServerErrorException;
import backend.academy.model.plain.Link;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

@Slf4j
@Component
@SuppressWarnings("PMD")
public class GitHubIssueListClient extends GitHubClient {
    private static final Pattern SUPPORTED_URL =
            Pattern.compile("^https://github.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/(issues|pulls)$");

    private final ApplicationStabilityProperties stabilityProperties;

    public GitHubIssueListClient(
            @Qualifier("gitHubIssueListClientConverter") LinkToApiLinkConverter linkConverter,
            @Qualifier("gitHubClient") RestClient restClient,
            ApplicationStabilityProperties stabilityProperties) {
        super(SUPPORTED_URL, linkConverter, restClient);
        this.stabilityProperties = stabilityProperties;
    }

    @Override
    @Retry(name = "default", fallbackMethod = "onErrorIssuesList")
    @CircuitBreaker(name = "default", fallbackMethod = "onCBErrorIssuesList")
    public List<LinkUpdateInfo> getUpdates(Link link) {
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
        List<LinkUpdateInfo> newIssues = createListOfNewIssue(issues, link);

        List<List<GitHubComment>> commentsForEachIssue = getCommentsForEachIssue(objectMapper, issues);
        List<LinkUpdateInfo> newComments = new ArrayList<>();
        for (List<GitHubComment> commentList : commentsForEachIssue) {
            if (commentList == null || commentList.isEmpty()) {
                continue;
            }
            newComments.addAll(createListOfCommentUpdates(objectMapper, link, commentList));
        }

        List<LinkUpdateInfo> resultList = new ArrayList<>();
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
            } else if (stabilityProperties
                    .getRetry()
                    .getHttpCodes()
                    .contains(response.getStatusCode().value())) {
                throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
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
            } else if (stabilityProperties
                    .getRetry()
                    .getHttpCodes()
                    .contains(response.getStatusCode().value())) {
                throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
            }
            log.atWarn()
                    .setMessage("Неудачный запрос на получение комментариев")
                    .addKeyValue("url", url)
                    .addKeyValue("code", response.getStatusCode())
                    .log();
            return null;
        });
    }

    private List<LinkUpdateInfo> createListOfCommentUpdates(
            ObjectMapper objectMapper, Link link, List<GitHubComment> comments) {
        List<LinkUpdateInfo> updates = new ArrayList<>();

        for (GitHubComment comment : comments) {
            if (issueWasUpdated(link.getLastUpdateTime(), comment.createdAt())) {
                GitHubIssue issue = getIssue(objectMapper, comment.issueUrl());
                updates.add(createNewCommentUpdate(comment, issue));
            }
        }
        return updates;
    }

    private List<LinkUpdateInfo> createListOfNewIssue(List<GitHubIssue> issues, Link link) {
        List<LinkUpdateInfo> issueUpdates = new ArrayList<>();

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
            } else if (stabilityProperties
                    .getRetry()
                    .getHttpCodes()
                    .contains(response.getStatusCode().value())) {
                throw new RetryableHttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
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

    private LinkUpdateInfo createNewCommentUpdate(GitHubComment comment, GitHubIssue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return new LinkUpdateInfo(
                comment.url(),
                comment.user().ownerName(),
                null,
                comment.body(),
                comment.createdAt(),
                String.format(
                        "Новый комментарий к issue %s%nАвтор: %s%nВремя создания: %s (UTC)%nПревью: %s",
                        issue.title(),
                        comment.user().ownerName(),
                        formatter.format(LocalDateTime.ofInstant(comment.createdAt(), ZoneId.of("UTC"))),
                        comment.body()));
    }

    private LinkUpdateInfo createNewIssueUpdate(GitHubIssue issue) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return new LinkUpdateInfo(
                issue.linkValue(),
                issue.author().ownerName(),
                issue.title(),
                issue.description(),
                issue.createdAt(),
                String.format(
                        "Новый issue %s%nАвтор: %s%nВремя создания: %s (UTC)%nПревью: %s",
                        issue.title(),
                        issue.author().ownerName(),
                        formatter.format(LocalDateTime.ofInstant(issue.createdAt(), ZoneId.of("UTC"))),
                        issue.description()));
    }

    public List<LinkUpdateInfo> onErrorIssuesList(Link link, Throwable t) {
        log.atWarn()
                .setMessage("Ошибка при получении обновлений isssue. Неудачный запрос")
                .addKeyValue("url", link.getUrl())
                .addKeyValue("exception", t.getMessage())
                .addKeyValue("stacktrace", t.getStackTrace())
                .log();
        return List.of();
    }

    public List<LinkUpdateInfo> onCBErrorIssuesList(Link link, Throwable e) {
        log.atWarn()
                .setMessage("Ошибка при получении обновлений isssue. Сервис недоступен")
                .addKeyValue("url", link.getUrl())
                .log();
        return List.of();
    }
}
