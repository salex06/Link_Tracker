package backend.academy.clients.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubUser(@JsonProperty("login") String ownerName, @JsonProperty("id") Long ownerId) {}
