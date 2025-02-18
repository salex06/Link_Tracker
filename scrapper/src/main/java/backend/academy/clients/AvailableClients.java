package backend.academy.clients;

import backend.academy.clients.github.repository.GitHubRepositoryClient;
import java.util.List;
import lombok.Getter;

public final class AvailableClients {
    @Getter
    private static final List<Client> availableClients = List.of(new GitHubRepositoryClient());

    private AvailableClients() {}
}
