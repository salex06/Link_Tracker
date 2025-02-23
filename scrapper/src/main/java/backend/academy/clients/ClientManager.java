package backend.academy.clients;

import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ClientManager {
    @Autowired
    @Getter
    private final List<Client> availableClients;

    public ClientManager(List<Client> availableClients) {
        this.availableClients = availableClients;
    }
}
