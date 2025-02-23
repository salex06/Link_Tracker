package backend.academy.clients;

import backend.academy.model.Link;
import java.util.List;

public interface Client {
    boolean supportLink(Link link);

    List<String> getUpdates(Link link);
}
