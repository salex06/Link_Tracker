package backend.academy.service;

import backend.academy.model.Link;
import backend.academy.repository.LinkRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

public class LinkService {
    private final LinkRepository linkRepository;

    @Autowired
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public List<Link> getAllLinks() {
        return linkRepository.getAllLinks();
    }

    public Optional<Link> getLink(Long id) {
        return linkRepository.getById(id);
    }

    public Link findLink(Link link) {
        List<Link> links = linkRepository.getAllLinks();
        for (Link dbLink : links) {
            if (Objects.equals(dbLink, link)) {
                return dbLink;
            }
        }
        return null;
    }

    public Link saveLink(Link link) {
        return linkRepository.save(link);
    }
}
