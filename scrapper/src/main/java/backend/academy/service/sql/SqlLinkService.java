package backend.academy.service.sql;

import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.JdbcLinkRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "service", name = "access-type", havingValue = "SQL")
public class SqlLinkService implements LinkService {
    private final JdbcLinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final ChatService chatService;

    @Autowired
    public SqlLinkService(JdbcLinkRepository linkRepository, LinkMapper linkMapper, ChatService chatService) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.chatService = chatService;
    }

    @Override
    public Iterable<Link> getAllLinks() {
        return null;
    }

    @Override
    public Optional<Link> getLinkById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Link> getLinkByValue(Long chatId, String url) {
        return Optional.empty();
    }

    @Override
    public Link saveLink(Link link) {
        return null;
    }

    @Override
    public Link updateChats(Link link, Set<TgChat> newChats) {
        return null;
    }

    @Override
    public Iterable<Link> getAllLinksByChatId(Long chatId) {
        return null;
    }
}
