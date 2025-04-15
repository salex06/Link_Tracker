package backend.academy.service.sql;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.repository.jdbc.JdbcChatRepository;
import backend.academy.service.ChatService;
import backend.academy.service.LinkService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

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
        JdbcTgChat newChat = chatRepository.save(new JdbcTgChat(null, chatId, null));
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
                    linkService.getAllLinksByChatId(jdbcTgChat.orElseThrow().getChatId())));
        }
        return plainChat;
    }

    @Override
    public void deleteChatByChatId(Long chatId) {
        chatRepository.deleteByChatId(chatId);
    }

    @Override
    public void updateTags(Link link, TgChat chat, List<String> tags) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.getChatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        JdbcTgChat jdbcChat = jdbcTgChat.orElseThrow();
        chatRepository.removeAllTags(link.getId(), jdbcChat.getId());
        for (String tag : new HashSet<>(tags)) {
            chatRepository.saveTag(link.getId(), jdbcChat.getId(), tag);
        }
        link.setTags(tags);
        chat.getLinks().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .forEach(i -> i.setTags(link.getTags()));
    }

    @Override
    public void updateFilters(Link link, TgChat chat, List<String> filters) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.getChatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        JdbcTgChat jdbcChat = jdbcTgChat.orElseThrow();
        chatRepository.removeAllFilters(link.getId(), jdbcChat.getId());
        for (String filter : new HashSet<>(filters)) {
            chatRepository.saveFilter(link.getId(), jdbcChat.getId(), filter);
        }
        link.setFilters(filters);
        chat.getLinks().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .forEach(i -> i.setFilters(link.getFilters()));
    }

    @Override
    public void saveTheChatLink(TgChat chat, Link link) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.getChatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        if (chat.getLinks().stream()
                .filter(i -> Objects.equals(i.getUrl(), link.getUrl()))
                .findAny()
                .isEmpty())
            chatRepository.saveTheChatLink(jdbcTgChat.orElseThrow().getId(), link.getId());
    }

    @Override
    public void removeTheChatLink(TgChat chat, Link link) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chat.getChatId());
        if (jdbcTgChat.isEmpty()) {
            return;
        }
        chatRepository.removeTheChatLink(jdbcTgChat.orElseThrow().getId(), link.getId());
        chatRepository.removeAllTags(link.getId(), jdbcTgChat.orElseThrow().getId());
        chatRepository.removeAllFilters(link.getId(), jdbcTgChat.orElseThrow().getId());
    }

    @Override
    public List<String> getTags(Long linkId, Long chatId) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chatId);
        if (jdbcTgChat.isEmpty()) {
            return List.of();
        }
        return chatRepository.getTags(linkId, jdbcTgChat.orElseThrow().getId());
    }

    @Override
    public List<String> getFilters(Long linkId, Long chatId) {
        Optional<JdbcTgChat> jdbcTgChat = chatRepository.findByChatId(chatId);
        if (jdbcTgChat.isEmpty()) {
            return List.of();
        }
        return chatRepository.getFilters(linkId, jdbcTgChat.orElseThrow().getId());
    }

    @Override
    public void addTagsToAllLinksByChatId(TgChat tgChat, List<String> tags) {
        JdbcTgChat jdbcTgChat = chatRepository.findByChatId(tgChat.getChatId()).orElseThrow();
        Set<Link> chatLinks = linkService.getAllLinksByChatId(jdbcTgChat.getChatId());
        for (String tag : tags) {
            for (Link link : chatLinks) {
                chatRepository.saveTag(link.getId(), jdbcTgChat.getId(), tag);
            }
        }
    }

    @Override
    public void removeTagsToAllLinksByChatId(TgChat tgChat, List<String> tags) {
        JdbcTgChat jdbcTgChat = chatRepository.findByChatId(tgChat.getChatId()).orElseThrow();
        Set<Link> chatLinks = linkService.getAllLinksByChatId(jdbcTgChat.getChatId());
        for (String tag : tags) {
            for (Link link : chatLinks) {
                chatRepository.removeTag(link.getId(), jdbcTgChat.getId(), tag);
            }
        }
    }

    @Override
    public boolean updateTimeConfig(TgChat tgChat, String timeConfig) {
        LocalTime config;
        if (timeConfig.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            String[] splittedConfig = timeConfig.split(":", 2);
            int hours = Integer.parseInt(splittedConfig[0]);
            int minutes = Integer.parseInt(splittedConfig[1]);
            config = LocalTime.of(hours, minutes);
        } else if (Objects.equals(timeConfig, "immediately")) {
            config = null;
        } else {
            return false;
        }

        chatRepository.updateTimeConfig(tgChat.getChatId(), config);
        return true;
    }

    @Override
    public List<Long> getChatIdsForImmediateDispatch(List<Long> chatIds) {
        List<Long> result = new ArrayList<>();
        for (Long id : chatIds) {
            Optional<JdbcTgChat> chat = chatRepository.findByChatId(id);
            if (chat.isEmpty()) continue;

            if (chat.orElseThrow().getSendAt() == null) {
                result.add(chat.orElseThrow().getChatId());
            }
        }
        return result;
    }

    @Override
    public List<Map.Entry<Long, LocalTime>> getChatIdsWithDelayedSending(List<Long> chatIds) {
        List<Map.Entry<Long, LocalTime>> result = new ArrayList<>();
        for (Long id : chatIds) {
            JdbcTgChat chat = chatRepository.findByChatId(id).orElse(null);
            if (chat == null) continue;

            if (chat.getSendAt() != null) {
                result.add(Map.entry(chat.getChatId(), chat.getSendAt()));
            }
        }
        return result;
    }
}
