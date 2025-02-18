package backend.academy.clients;

import backend.academy.model.Link;

public interface Client {
    boolean supportLink(Link link);

    String getUpdates(Link link);
}
