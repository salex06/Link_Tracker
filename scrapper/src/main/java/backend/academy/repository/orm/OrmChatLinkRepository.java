package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChatLink;
import backend.academy.model.orm.OrmChatLinkIdEmbedded;
import backend.academy.model.orm.OrmLink;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией о связи чата с ссылкой */
@Repository
public interface OrmChatLinkRepository extends JpaRepository<OrmChatLink, OrmChatLinkIdEmbedded> {
    /**
     * Найти все чаты, связанные с ссылкой
     *
     * @param linkId идентификатор ссылки
     * @return набор внутренних идентификаторов чатов
     */
    @Query("SELECT cl.chat.id FROM OrmChatLink cl WHERE cl.link.id = :linkId")
    Set<Long> findAllChatIdByLinkId(@Param("linkId") Long linkId);

    /**
     * Найти все ссылки по внутреннему идентификатору чата
     *
     * @param chatId идентификатор чата (внутренний)
     * @return набор ссылок, связанных с чатом
     */
    @Query("SELECT cl.link FROM OrmChatLink cl WHERE cl.chat.id = :chatId")
    List<OrmLink> findAllByChatPrimaryId(Long chatId);

    /**
     * Найти связанную с чатом ссылку по идентификатору чата и по значению ссылки
     *
     * @param chatId идентификатор чата (внутренний)
     * @param linkValue значение ссылки
     * @return {@code Optional<OrmLink>}, если такая ссылка найдена, иначе - {@code Optional.empty()}
     */
    @Query("SELECT cl.link FROM OrmChatLink cl WHERE cl.chat.id = :chatId AND cl.link.linkValue = :linkValue")
    Optional<OrmLink> findByChatPrimaryIdAndLinkValue(
            @Param("chatId") Long chatId, @Param("linkValue") String linkValue);
}
