package backend.academy.clients.github.issues;

import backend.academy.clients.converter.JsonConverters;
import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

/**
 * Модель комментария в issue или pull request на GitHub
 *
 * @param id идентификатор комментария
 * @param url ссылка на комментарий
 * @param user автор комментария
 * @param createdAt дата создания комментария
 * @param body тело комментария
 * @param issueUrl issue или pull request, на которые оставлен комментарий
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubComment(
        @JsonProperty("id") Long id,
        @JsonProperty("html_url") String url,
        @JsonProperty("user") GitHubUser user,
        @JsonProperty("created_at") @JsonDeserialize(converter = JsonConverters.InstantTimeConverter.class)
                Instant createdAt,
        @JsonProperty("body") @JsonDeserialize(converter = JsonConverters.StringTruncator.class) String body,
        @JsonProperty("issue_url") String issueUrl) {}
