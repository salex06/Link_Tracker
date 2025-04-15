package backend.academy.service;

import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatService {
    TgChat saveChat(Long chatId);

    Optional<TgChat> getPlainTgChatByChatId(Long chatId);

    boolean containsChat(Long chatId);

    void deleteChatByChatId(Long chatId);

    void updateTags(Link link, TgChat chat, List<String> tags);

    void updateFilters(Link link, TgChat chat, List<String> filters);

    /**
     * Связать ссылку с чатом (добавить ссылку как отслеживаемую данным чатом)
     *
     * @param chat чат
     * @param link ссылка
     */
    void saveTheChatLink(TgChat chat, Link link);

    /**
     * Удалить связь между ссылкой и чатом (прекратить отслеживание ссылки чатом)
     *
     * @param chat чат
     * @param link ссылка
     */
    void removeTheChatLink(TgChat chat, Link link);

    List<String> getTags(Long linkId, Long chatId);

    List<String> getFilters(Long linkId, Long chatId);

    void addTagsToAllLinksByChatId(TgChat tgChat, List<String> tags);

    void removeTagsToAllLinksByChatId(TgChat tgChat, List<String> tags);

    boolean updateTimeConfig(TgChat tgChat, String timeConfig);

    /**
     * Извлечь идентификаторы чатов с режимом немедленной отправки
     *
     * @param chatIds набор идентификаторов с разными режимами
     * @return список идентификаторов с режимом немедленной отправки уведомлений
     */
    List<Long> getChatIdsForImmediateDispatch(List<Long> chatIds);

    /**
     * Извлечь идентификаторы чатов с отправкой уведомлений в режиме дайджеста
     *
     * @param chatIds набор идентификаторов с разными режимами
     * @return набор пар "идентификатор - время отправки"
     */
    List<Map.Entry<Long, LocalTime>> getChatIdsWithDelayedSending(List<Long> chatIds);
}
