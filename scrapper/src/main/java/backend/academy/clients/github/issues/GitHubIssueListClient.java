package backend.academy.clients.github.issues;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.dto.LinkUpdateInfo;
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
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubIssueListClient extends Client {
    private static final Pattern SUPPORTED_URL =
            Pattern.compile("^https://github.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)/(issues|pulls)$");

    private final RetryTemplate retryTemplate;

    public GitHubIssueListClient(
            @Qualifier("gitHubIssueListClientConverter") LinkToApiLinkConverter linkConverter,
            @Qualifier("gitHubClient") RestClient restClient,
            RetryTemplate retryTemplate) {
        super(SUPPORTED_URL, linkConverter, restClient);
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
        return retryTemplate.execute(
                context -> client.get().uri(url).exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
                    } else if (response.getStatusCode().isError()) {
                        throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                    }

                    log.atWarn()
                            .setMessage("Неудачный запрос на получение issues/pull requests")
                            .addKeyValue("url", url)
                            .addKeyValue("code", response.getStatusCode())
                            .log();
                    return null;
                }),
                context -> null);
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
        return retryTemplate.execute(
                context -> client.get().uri(url).exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return mapper.readValue(response.getBody(), new TypeReference<>() {});
                    } else if (response.getStatusCode().isError()) {
                        throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                    }
                    log.atWarn()
                            .setMessage("Неудачный запрос на получение комментариев")
                            .addKeyValue("url", url)
                            .addKeyValue("code", response.getStatusCode())
                            .log();
                    return null;
                }),
                context -> null);
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
        return retryTemplate.execute(
                context -> client.get().uri(issueUrl).exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return mapper.readValue(response.getBody(), GitHubIssue.class);
                    } else if (response.getStatusCode().isError()) {
                        throw new HttpServerErrorException(response.getStatusCode(), "Ошибка сервера");
                    }

                    log.atWarn()
                            .setMessage("Неудачный запрос на получение issue")
                            .addKeyValue("url", issueUrl)
                            .addKeyValue("code", response.getStatusCode())
                            .log();
                    return null;
                }),
                context -> null);
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
}
