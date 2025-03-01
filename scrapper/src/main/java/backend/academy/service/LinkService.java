package backend.academy.service;

import backend.academy.model.Link;
import backend.academy.repository.LinkRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public Link saveOrGetLink(Link link) {
        Link inDataBaseLink = findLink(link);
        if (inDataBaseLink != null) {
            return inDataBaseLink;
        }
        saveLink(link);
        return link;
    }

    public boolean appendChatToLink(Long chatId, Link source) {
        Optional<Link> linkFromDatabaseWrapper = linkRepository.getById(source.getId());
        if (linkFromDatabaseWrapper.isPresent()) {
            Link link = linkFromDatabaseWrapper.orElseThrow();
            Set<Long> chats = link.getTgChatIds();
            chats.add(chatId);
            link.setTgChatIds(chats);
            linkRepository.save(link);
            return true;
        }
        return false;
    }

    public boolean deleteChatFromLink(Long chatId, Link link) {
        Link databaseLink = findLink(link);
        if (databaseLink == null) {
            return false;
        }

        Set<Long> tgChatIds = link.getTgChatIds();
        if (tgChatIds.remove(chatId)) {
            link.setTgChatIds(tgChatIds);
            linkRepository.save(link);
            return true;
        }
        return false;
    }
}
