package backend.academy.clients.github.repository;

import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record GitHubRepositoryDTO(
        @JsonProperty("html_url") String linkValue,
        @JsonProperty("name") String repositoryName,
        @JsonProperty("owner") GitHubUser owner,
        @JsonProperty("pushed_at") LocalDateTime pushedAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt) {}
