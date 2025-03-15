package backend.academy.service;

import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Optional;

public interface ChatService {
    TgChat saveChat(Long chatId);

    Optional<TgChat> getPlainTgChatByChatId(Long chatId);

    boolean containsChat(Long chatId);

    void deleteChatByChatId(Long chatId);

    void updateTags(Link link, TgChat chat, List<String> tags);

    void updateFilters(Link link, TgChat chat, List<String> filters);

    void saveTheChatLink(TgChat chat, Link link);

    void removeTheChatLink(TgChat chat, Link link);

    List<String> getTags(Long linkId, Long chatId);

    List<String> getFilters(Long linkId, Long chatId);
}
