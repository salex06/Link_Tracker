package backend.academy.service.sql;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.JdbcChatRepository;
import backend.academy.repository.JdbcLinkRepository;
import backend.academy.service.LinkService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<Link> getAllLinks(Pageable pageable) {
        List<Link> plainLinks = new ArrayList<>();
        Page<JdbcLink> jdbcLinks = linkRepository.findAll(pageable);

        for (JdbcLink link : jdbcLinks) {
            Set<Long> chats = linkRepository.getChatIdsByUrl(link.getUrl());
            plainLinks.add(linkMapper.toPlainLink(link, null, null, chats));
        }

        return new PageImpl<Link>(plainLinks, pageable, jdbcLinks.getTotalElements());
    }

    @Override
    public Optional<Link> getLink(Long chatId, String linkValue) {
        Optional<JdbcLink> jdbcLink = chatRepository.getLinkByValueAndChatId(linkValue, chatId);
        if (jdbcLink.isEmpty()) {
            return Optional.empty();
        }

        JdbcLink link = jdbcLink.orElseThrow();
        List<String> tags = chatRepository.getTags(link.getId(), chatId);
        List<String> filters = chatRepository.getFilters(link.getId(), chatId);
        Set<Long> chats = getChatIdsListeningToLink(link.getUrl());

        return Optional.of(linkMapper.toPlainLink(link, tags, filters, chats));
    }

    @Override
    public Link saveLink(Link link, TgChat chat) {
        JdbcLink savedLink = linkRepository
                .getLinkByUrl(link.getUrl())
                .orElseGet(() -> linkRepository.save(linkMapper.toJdbcLink(link)));

        chatRepository.saveTheChatLink(chat.chatId(), savedLink.getId());

        if (!link.getTags().isEmpty()) {
            for (String tag : link.getTags()) {
                chatRepository.saveTag(savedLink.getId(), chat.chatId(), tag);
            }
        }

        if (!link.getFilters().isEmpty()) {
            for (String filter : link.getFilters()) {
                chatRepository.saveFilter(savedLink.getId(), chat.chatId(), filter);
            }
        }

        List<String> tags = chatRepository.getTags(chat.chatId(), savedLink.getId());
        List<String> filter = chatRepository.getFilters(chat.chatId(), savedLink.getId());
        Set<Long> chats = chatRepository.getChatsByLink(savedLink.getId()).stream()
                .map(JdbcTgChat::chatId)
                .collect(Collectors.toSet());

        Link plainLink = linkMapper.toPlainLink(savedLink, tags, filter, chats);
        chat.addLink(plainLink);

        return plainLink;
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
