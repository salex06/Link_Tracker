package backend.academy.service.sql;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.JdbcChatRepository;
import backend.academy.repository.JdbcLinkRepository;
import backend.academy.service.LinkService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "service", name = "access-type", havingValue = "SQL")
public class SqlLinkService implements LinkService {
    private final JdbcLinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final JdbcChatRepository chatRepository;

    @Autowired
    public SqlLinkService(JdbcLinkRepository linkRepository, LinkMapper linkMapper, JdbcChatRepository chatRepository) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.chatRepository = chatRepository;
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
    public Set<Link> getAllLinksByChatId(Long chatId) {
        Set<Link> plainLinks = new HashSet<>();
        List<JdbcLink> jdbcLinks = linkRepository.getAllLinksByChatId(chatId);
        for (JdbcLink link : jdbcLinks) {
            List<String> tags = chatRepository.getTags(link.getId(), chatId);
            List<String> filters = chatRepository.getFilters(link.getId(), chatId);
            Set<Long> chats = getChatIdsListeningToLink(link.getUrl());
            plainLinks.add(linkMapper.toPlainLink(link, tags, filters, chats));
        }
        return plainLinks;
    }

    @Override
    public Set<Long> getChatIdsListeningToLink(String url) {
        Optional<JdbcLink> jdbcLink = chatRepository.getLinkByValue(url);
        if (jdbcLink.isEmpty()) {
            return Set.of();
        }
        JdbcLink link = jdbcLink.orElseThrow();
        List<JdbcTgChat> chats = chatRepository.getChatsByLink(link.getId());

        return chats.stream().map(JdbcTgChat::chatId).collect(Collectors.toSet());
    }
}
