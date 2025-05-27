package backend.academy.clients;

import backend.academy.model.plain.Link;
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

    public Client getSuitableClient(Link link) {
        for (Client client : availableClients) {
            if (client.supportLink(link.getUrl())) {
                return client;
            }
        }
        return null;
    }
}
