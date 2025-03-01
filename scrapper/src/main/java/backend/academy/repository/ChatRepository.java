package backend.academy.repository;

import backend.academy.model.TgChat;
import java.util.List;
import java.util.Optional;

/** Интерфейс предоставляет набор методов для доступа к данным из БД о чатах пользователей с ботом */
public interface ChatRepository {
    /**
     * Получить чат по его идентификатору
     *
     * @param id идентификатор чата
     * @return {@code Optional<TgChat>} если чат найден, иначе - {@code Optional.empty()}
     */
    Optional<TgChat> getById(Long id);

    /**
     * Получить список всех чатов из базы данных
     *
     * @return список сохраненных чатов
     */
    List<TgChat> getAll();

    /**
     * Сохранить чат по его идентификатору
     *
     * @param id идентификатор чата
     * @return {@code true}, если чат успешно сохранен, иначе - false
     */
    boolean save(Long id);

    /**
     * Сохранить чат
     *
     * @param tgChat чат, который требуется сохранить
     * @return {@code TgChat} - объект класса TgChat - предыдущее состояние чата (null, если добавляется впервые)
     */
    TgChat saveTgChat(TgChat tgChat);

    /**
     * Удалить чат по его идентификатору
     *
     * @param id идентификатор чата
     * @return {@code true}, если чат успешно удален, иначе - false
     */
    boolean remove(Long id);
}
