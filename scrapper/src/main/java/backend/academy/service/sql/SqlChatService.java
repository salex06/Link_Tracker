package backend.academy.service.sql;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.JdbcChatRepository;
import backend.academy.service.ChatService;
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
public class SqlChatService implements ChatService {
    @Autowired
    private JdbcChatRepository chatRepository;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private LinkMapper linkMapper;

    @Override
    public TgChat saveChat(Long chatId) {
        if (containsChat(chatId)) {
            return null;
        }
        JdbcTgChat newChat = chatRepository.save(new JdbcTgChat(null, chatId));
        return chatMapper.toPlainTgChat(newChat, new HashSet<>());
    }

    @Override
    public Optional<TgChat> getChatByChatId(Long chatId) {
        Optional<TgChat> plainChat = Optional.empty();
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chatId);
        if (jdbcTgChat.isPresent()) {
            plainChat = Optional.of(new TgChat(
                    jdbcTgChat.orElseThrow().id(), jdbcTgChat.orElseThrow().chatId(), getAllLinksByChatId(chatId)));
        }
        return plainChat;
    }

    @Override
    public void deleteChatByChatId(Long chatId) {
        chatRepository.deleteByChatId(chatId);
    }

    @Override
    public boolean containsChat(Long chatId) {
        return chatRepository.existsByChatId(chatId);
    }

    @Override
    public Set<Link> getAllLinksByChatId(Long chatId) {
        Set<Link> plainLinks = new HashSet<>();
        List<JdbcLink> jdbcLinks = chatRepository.getAllLinksByChatId(chatId);
        for (JdbcLink link : jdbcLinks) {
            List<String> tags = chatRepository.getTags(link.getId(), chatId);
            List<String> filters = chatRepository.getFilters(link.getId(), chatId);
            Set<Long> chats = getChatIdsListeningToLink(link.getUrl());
            plainLinks.add(linkMapper.toPlainLink(link, tags, filters, chats));
        }
        return plainLinks;
    }

    private Set<Long> getChatIdsListeningToLink(String url) {
        Optional<JdbcLink> jdbcLink = chatRepository.getLinkByValue(url);
        if (jdbcLink.isEmpty()) {
            return Set.of();
        }
        JdbcLink link = jdbcLink.orElseThrow();
        List<JdbcTgChat> chats = chatRepository.getChatsByLink(link.getId());

        return chats.stream().map(JdbcTgChat::chatId).collect(Collectors.toSet());
    }

    @Override
    public Optional<Link> getLink(Long chatId, String linkValue) {
        Optional<JdbcLink> jdbcLink = chatRepository.getLinkByValue(linkValue);
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
    public Link saveLink(Long chatId, Link link) {
        JdbcLink savedLink = chatRepository.saveLink(link.getUrl());
        chatRepository.saveTheChatLink(chatId, savedLink.getId());
        if (!link.getTags().isEmpty()) {
            for (String tag : link.getTags()) {
                chatRepository.saveTag(savedLink.getId(), chatId, tag);
            }
        }

        if (!link.getFilters().isEmpty()) {
            for (String filter : link.getFilters()) {
                chatRepository.saveFilter(savedLink.getId(), chatId, filter);
            }
        }

        List<String> tags = chatRepository.getTags(chatId, savedLink.getId());
        List<String> filter = chatRepository.getFilters(chatId, savedLink.getId());
        Set<Long> chats = chatRepository.getChatsByLink(savedLink.getId()).stream()
                .map(JdbcTgChat::chatId)
                .collect(Collectors.toSet());

        return linkMapper.toPlainLink(savedLink, tags, filter, chats);
    }

    @Override
    public void updateTags(Link link, TgChat chat, List<String> tags) {
        for (String tag : tags) {
            chatRepository.removeAllTags(link.getId(), chat.chatId());
            chatRepository.saveTag(link.getId(), chat.chatId(), tag);
        }
    }

    @Override
    public void updateFilters(Link link, TgChat chat, List<String> filters) {
        for (String filter : filters) {
            chatRepository.removeAllFilters(link.getId(), chat.chatId());
            chatRepository.saveFilter(link.getId(), chat.chatId(), filter);
        }
    }

    @Override
    public void saveTheChatLink(TgChat chat, Link link) {
        chatRepository.saveTheChatLink(chat.chatId(), link.getId());
    }

    @Override
    public void removeTheChatLink(TgChat chat, Link link) {
        chatRepository.removeTheChatLink(chat.chatId(), link.getId());
    }

    @Override
    public JdbcTgChat updateLinks(JdbcTgChat chat, Set<JdbcLink> newLinks) {
        // TODO: реализовать обновление ссылок
        return chatRepository.save(chat);
    }

    @Override
    public List<JdbcTgChat> getChatsListeningToLink(JdbcLink link) {
        return chatRepository.getChatsByLink(link.getId());
    }

    @Override
    public List<String> getTags(Link link, TgChat chat) {
        return chatRepository.getTags(link.getId(), chat.chatId());
    }

    @Override
    public List<String> getFilters(Link link, TgChat chat) {
        return chatRepository.getFilters(link.getId(), chat.chatId());
    }
}
