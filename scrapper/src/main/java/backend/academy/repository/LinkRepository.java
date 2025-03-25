package backend.academy.repository;

import backend.academy.model.Link;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    Optional<Link> getById(Long id);

    List<Link> getAllLinks();

    Link save(Link link);
}
