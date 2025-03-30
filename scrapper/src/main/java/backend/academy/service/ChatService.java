package backend.academy.service;

import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс содержит набор методов, реализующих бизнес-логику обработки информации о чатах (теги, связанные ссылки,
 * фильтры)
 */
public interface ChatService {
    /**
     * Сохранить чат
     *
     * @param chatId идентификатор чата от telegram Api
     * @return сохраненный чат
     */
    TgChat saveChat(Long chatId);

    /**
     * Получить чат по его идентификатору
     *
     * @param chatId идентификатор чата от telegram Api
     * @return {@code Optional<TgChat>}, если чат найден, иначе {@code Optional.empty()}
     */
    Optional<TgChat> getPlainTgChatByChatId(Long chatId);

    /**
     * Проверить, содержится ли чат в БД
     *
     * @param chatId идентификатор чата от telegram Api
     * @return {@code true}, если чат содержится в БД, иначе {@code false}
     */
    boolean containsChat(Long chatId);

    /**
     * Удалить чат
     *
     * @param chatId идентификатор чата
     */
    void deleteChatByChatId(Long chatId);

    /**
     * Обновить теги для ссылки, связанной с чатом
     *
     * @param link ссылка
     * @param chat чат
     * @param tags новые теги
     */
    void updateTags(Link link, TgChat chat, List<String> tags);

    /**
     * Обновить фильтры для ссылки, связанной с чатом
     *
     * @param link ссылка
     * @param chat чат
     * @param filters новые фильтры
     */
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

    /**
     * Получить теги для данной ссылки и чата
     *
     * @param linkId идентификатор ссылки
     * @param chatId идентификатор чата от telegram Api
     * @return список тегов
     */
    List<String> getTags(Long linkId, Long chatId);

    /**
     * Получить фильтры для данной ссылки и чата
     *
     * @param linkId идентификатор ссылки
     * @param chatId идентификатор чата от telegram Api
     * @return список фильтров
     */
    List<String> getFilters(Long linkId, Long chatId);

    /**
     * Добавить теги ко всем ссылкам чата
     *
     * @param tgChat чат
     * @param tags набор тегов, которые требуется добавить
     */
    void addTagsToAllLinksByChatId(TgChat tgChat, List<String> tags);
}
