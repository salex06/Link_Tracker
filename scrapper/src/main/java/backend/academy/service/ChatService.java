package backend.academy.service;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatService {
    TgChat saveChat(Long chatId);

    Link saveLink(Long chatId, Link link);

    Optional<TgChat> getPlainTgChatByChatId(Long chatId);

    boolean containsChat(Long chatId);

    void deleteChatByChatId(Long chatId);

    Optional<Link> getLink(Long chatId, String linkValue);

    void updateTags(Link link, TgChat chat, List<String> tags);

    void updateFilters(Link link, TgChat chat, List<String> filters);

    void saveTheChatLink(TgChat chat, Link link);

    void removeTheChatLink(TgChat chat, Link link);

    JdbcTgChat updateLinks(JdbcTgChat chat, Set<JdbcLink> newLinks);

    List<JdbcTgChat> getChatsListeningToLink(JdbcLink link);

    List<String> getTags(Long linkId, Long chatId);

    List<String> getFilters(Long linkId, Long chatId);
}
