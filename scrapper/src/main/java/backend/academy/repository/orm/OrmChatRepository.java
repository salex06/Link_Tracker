package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Репозиторий для взаимодействия с информацией о чатах, сохраненных в БД */
@Repository
public interface OrmChatRepository extends JpaRepository<OrmChat, Long> {
    /**
     * Найти чат по идентификатору Telegram Api
     *
     * @param tgChatId идентификатор, предоставляемый Telegram Api
     * @return {@code Optional<OrmChat>}, если чат найден, иначе - {@code Optional.empty()}
     */
    Optional<OrmChat> findByChatId(Long tgChatId);

    /**
     * Проверить, существует ли чат в БД
     *
     * @param tgChatId идентификатор, предоставляемый Telegram Api
     * @return {@code true}, если чат найден, иначе - {@code false}
     */
    boolean existsByChatId(Long tgChatId);

    /**
     * Удалить чат по идентификатору
     *
     * @param chatId идентификатор, предоставляемый Telegram Api
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OrmChat c WHERE c.chatId = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);
}
