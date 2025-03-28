package backend.academy.clients.github.issues;

import backend.academy.clients.converter.JsonConverters;
import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

/**
 * Модель задачи (issue) GitHub
 *
 * @param linkValue ссылка на issue
 * @param title название
 * @param author автор
 * @param createdAt время создания
 * @param updatedAt время обновления
 * @param description описание issue
 * @param commentsUrl ссылка на комментарии к issue
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubIssue(
        @JsonProperty("html_url") String linkValue,
        @JsonProperty("title") String title,
        @JsonProperty("user") GitHubUser author,
        @JsonProperty("created_at") @JsonDeserialize(converter = JsonConverters.InstantTimeConverter.class)
                Instant createdAt,
        @JsonProperty("updated_at") @JsonDeserialize(converter = JsonConverters.InstantTimeConverter.class)
                Instant updatedAt,
        @JsonProperty("body") String description,
        @JsonProperty("comments_url") String commentsUrl) {}
