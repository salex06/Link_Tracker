package backend.academy.clients.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubUser(@JsonProperty("login") String ownerName, @JsonProperty("id") Long ownerId) {}
