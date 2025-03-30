package backend.academy.service.sql;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.jdbc.JdbcChatRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Сервис для реализации бизнес-логики взаимодействия с чатами */
@Service("sqlChatService")
@ConditionalOnProperty(prefix = "service", name = "access-type", havingValue = "SQL")
public class SqlChatService implements ChatService {
    private final JdbcChatRepository chatRepository;
    private final ChatMapper chatMapper;
    private final LinkService linkService;

    @Autowired
    public SqlChatService(
            JdbcChatRepository chatRepository,
            ChatMapper chatMapper,
            @Qualifier("sqlLinkService") LinkService linkService) {
        this.chatRepository = chatRepository;
        this.chatMapper = chatMapper;
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
            plainChat = Optional.of(chatMapper.toPlainTgChat(
                    jdbcTgChat.orElseThrow(),
                    linkService.getAllLinksByChatId(jdbcTgChat.orElseThrow().chatId())));
        }
        return plainChat;
    }

    @Override
    public void deleteChatByChatId(Long chatId) {
        chatRepository.deleteByChatId(chatId);
    }

    @Override
    public void updateTags(Link link, TgChat chat, List<String> tags) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.chatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        JdbcTgChat jdbcChat = jdbcTgChat.orElseThrow();
        chatRepository.removeAllTags(link.getId(), jdbcChat.id());
        for (String tag : new HashSet<>(tags)) {
            chatRepository.saveTag(link.getId(), jdbcChat.id(), tag);
        }
        link.setTags(tags);
        chat.links().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .forEach(i -> i.setTags(link.getTags()));
    }

    @Override
    public void updateFilters(Link link, TgChat chat, List<String> filters) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.chatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        JdbcTgChat jdbcChat = jdbcTgChat.orElseThrow();
        chatRepository.removeAllFilters(link.getId(), jdbcChat.id());
        for (String filter : new HashSet<>(filters)) {
            chatRepository.saveFilter(link.getId(), jdbcChat.id(), filter);
        }
        link.setFilters(filters);
        chat.links().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .forEach(i -> i.setFilters(link.getFilters()));
    }

    @Override
    public void saveTheChatLink(TgChat chat, Link link) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.chatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        if (chat.links().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .findAny()
                .isEmpty())
            chatRepository.saveTheChatLink(jdbcTgChat.orElseThrow().id(), link.getId());
    }

    @Override
    public void removeTheChatLink(TgChat chat, Link link) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.chatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        chatRepository.removeTheChatLink(jdbcTgChat.orElseThrow().id(), link.getId());
        chatRepository.removeAllTags(link.getId(), jdbcTgChat.orElseThrow().id());
        chatRepository.removeAllFilters(link.getId(), jdbcTgChat.orElseThrow().id());
    }

    @Override
    public List<String> getTags(Long linkId, Long chatId) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chatId);
        if (jdbcTgChat.isEmpty()) {
            return List.of();
        }
        return chatRepository.getTags(linkId, jdbcTgChat.orElseThrow().id());
    }

    @Override
    public List<String> getFilters(Long linkId, Long chatId) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chatId);
        if (jdbcTgChat.isEmpty()) {
            return List.of();
        }
        return chatRepository.getFilters(linkId, jdbcTgChat.orElseThrow().id());
    }

    @Override
    public void addTagsToAllLinksByChatId(TgChat tgChat, List<String> tags) {
        JdbcTgChat jdbcTgChat = chatRepository.findByChatId(tgChat.chatId()).orElseThrow();
        Set<Link> chatLinks = linkService.getAllLinksByChatId(jdbcTgChat.chatId());
        for (String tag : tags) {
            for (Link link : chatLinks) {
                chatRepository.saveTag(link.getId(), jdbcTgChat.id(), tag);
            }
        }
    }

    @Override
    public void removeTagsToAllLinksByChatId(TgChat tgChat, List<String> tags) {
        JdbcTgChat jdbcTgChat = chatRepository.findByChatId(tgChat.chatId()).orElseThrow();
        Set<Link> chatLinks = linkService.getAllLinksByChatId(jdbcTgChat.chatId());
        for (String tag : tags) {
            for (Link link : chatLinks) {
                chatRepository.removeTag(link.getId(), jdbcTgChat.id(), tag);
            }
        }
    }
}
