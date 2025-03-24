package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChatLinkTags;
import backend.academy.model.orm.OrmChatLinkTagsIdEmbedded;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией из БД о тегах, связанных с чатом и ссылкой */
@Repository
public interface OrmChatLinkTagsRepository extends JpaRepository<OrmChatLinkTags, OrmChatLinkTagsIdEmbedded> {
    /**
     * Удалить тег, связанный с чатом и ссылкой
     *
     * @param chatId идентификатор чата (внутренний)
     * @param linkId идентификатор ссылки
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM OrmChatLinkTags t WHERE t.chat.id = :chatId AND t.link.id = :linkId")
    void deleteByChatPrimaryIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);

    /**
     * Найти теги, связанные с ссылкой и чатом
     *
     * @param chatId идентификатор чата (внутренний)
     * @param linkId идентификатор ссылки
     * @return набор строк-тегов
     */
    @Query("SELECT t.id.tagValue FROM OrmChatLinkTags t WHERE t.chat.id = :chatId AND t.link.id = :linkId")
    List<String> findTagValuesByChatPrimaryIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);
}
