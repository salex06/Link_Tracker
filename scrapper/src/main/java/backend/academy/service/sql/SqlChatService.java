package backend.academy.service.sql;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.JdbcChatRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "service", name = "access-type", havingValue = "SQL")
public class SqlChatService implements ChatService {
    private final JdbcChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final LinkMapper linkMapper;
    private final LinkService linkService;

    @Autowired
    public SqlChatService(
            JdbcChatRepository chatRepository, ChatMapper chatMapper, LinkMapper linkMapper, LinkService linkService) {
        this.chatRepository = chatRepository;
        this.chatMapper = chatMapper;
        this.linkMapper = linkMapper;
        this.linkService = linkService;
    }

    @Override
    public TgChat saveChat(Long chatId) {
        if (containsChat(chatId)) {
            return null;
        }
        JdbcTgChat newChat = chatRepository.save(new JdbcTgChat(null, chatId));
        return chatMapper.toPlainTgChat(newChat, new HashSet<>());
    }

    @Override
    public boolean containsChat(Long chatId) {
        return chatRepository.existsByChatId(chatId);
    }

    @Override
    public Optional<TgChat> getPlainTgChatByChatId(Long chatId) {
        Optional<TgChat> plainChat = Optional.empty();
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chatId);
        if (jdbcTgChat.isPresent()) {
            plainChat = Optional.of(new TgChat(
                    jdbcTgChat.orElseThrow().id(),
                    jdbcTgChat.orElseThrow().chatId(),
                    linkService.getAllLinksByChatId(chatId)));
        }
        return plainChat;
    }

    @Override
    public void deleteChatByChatId(Long chatId) {
        chatRepository.deleteByChatId(chatId);
    }

    @Override
    public void updateTags(Link link, TgChat chat, List<String> tags) {
        chatRepository.removeAllTags(link.getId(), chat.chatId());
        for (String tag : tags) {
            chatRepository.saveTag(link.getId(), chat.chatId(), tag);
        }
        link.setTags(tags);
        chat.links().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .forEach(i -> i.setTags(link.getTags()));
    }

    @Override
    public void updateFilters(Link link, TgChat chat, List<String> filters) {
        chatRepository.removeAllFilters(link.getId(), chat.chatId());
        for (String filter : filters) {
            chatRepository.saveFilter(link.getId(), chat.chatId(), filter);
        }
        link.setFilters(filters);
        chat.links().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .forEach(i -> i.setFilters(link.getFilters()));
    }

    @Override
    public void saveTheChatLink(TgChat chat, Link link) {
        if (chat.links().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .findAny()
                .isEmpty()) chatRepository.saveTheChatLink(chat.chatId(), link.getId());
    }

    @Override
    public void removeTheChatLink(TgChat chat, Link link) {
        chatRepository.removeTheChatLink(chat.chatId(), link.getId());
    }

    @Override
    public List<String> getTags(Long linkId, Long chatId) {
        return chatRepository.getTags(linkId, chatId);
    }

    @Override
    public List<String> getFilters(Long linkId, Long chatId) {
        return chatRepository.getFilters(linkId, chatId);
    }
}
