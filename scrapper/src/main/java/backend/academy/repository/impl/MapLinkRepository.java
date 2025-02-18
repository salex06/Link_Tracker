package backend.academy.repository.impl;

import backend.academy.model.Link;
import backend.academy.repository.LinkRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MapLinkRepository implements LinkRepository {
    private final Map<Long, Link> database = new HashMap<>();

    @Override
    public Optional<Link> getById(Long id) {
        return Optional.of(database.get(id));
    }

    @Override
    public List<Link> getAllLinks() {
        return database.values().stream().toList();
    }

    @Override
    public Link save(Link link) {
        Link previousLink = null;
        if (database.containsKey(link.getId())) {
            previousLink = database.get(link.getId());
        }
        database.put(link.getId(), link);
        return previousLink;
    }
}
