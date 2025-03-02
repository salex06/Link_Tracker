package backend.academy.clients.github.repository;

import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Модель репозитория GitHub
 *
 * @param linkValue ссылка на репозиторий
 * @param repositoryName название репозитория
 * @param owner владелец репозитория
 * @param pushedAt время добавления нового коммита в репозиторий
 * @param updatedAt время обновления репозитория
 */
public record GitHubRepositoryDTO(
        @JsonProperty("html_url") String linkValue,
        @JsonProperty("name") String repositoryName,
        @JsonProperty("owner") GitHubUser owner,
        @JsonProperty("pushed_at") LocalDateTime pushedAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt) {}
