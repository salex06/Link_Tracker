package backend.academy.clients.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RepositoryOwner(@JsonProperty("login") String ownerName, @JsonProperty("id") Long ownerId) {}
