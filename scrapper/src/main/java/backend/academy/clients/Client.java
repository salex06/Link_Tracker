package backend.academy.clients;

import backend.academy.model.Link;
import java.util.List;
import org.springframework.web.client.RestClient;

public interface Client {
    boolean supportLink(Link link);

    List<String> getUpdates(Link link, RestClient client);
}
