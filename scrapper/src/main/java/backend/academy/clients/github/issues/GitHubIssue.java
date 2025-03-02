package backend.academy.clients.github.issues;

import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Модель задачи (issue) GitHub
 *
 * @param linkValue ссылка на issue
 * @param title название
 * @param author автор
 * @param updatedAt время обновления
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubIssue(
        @JsonProperty("html_url") String linkValue,
        @JsonProperty("title") String title,
        @JsonProperty("user") GitHubUser author,
        @JsonProperty("updated_at") LocalDateTime updatedAt) {}
