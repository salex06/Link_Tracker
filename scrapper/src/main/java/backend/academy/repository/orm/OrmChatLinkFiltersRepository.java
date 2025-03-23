package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChatLinkFilters;
import backend.academy.model.orm.OrmChatLinkFiltersIdEmbedded;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией о фильтрах, связанных с чатом и ссылкой */
@Repository
public interface OrmChatLinkFiltersRepository extends JpaRepository<OrmChatLinkFilters, OrmChatLinkFiltersIdEmbedded> {
    @Transactional
    @Modifying
    @Query("DELETE FROM OrmChatLinkFilters t WHERE t.chat.id = :chatId AND t.link.id = :linkId")
    void deleteByChatPrimaryIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);

    @Query("SELECT t.id.filterValue FROM OrmChatLinkFilters t WHERE t.chat.id = :chatId AND t.link.id = :linkId")
    List<String> findFilterValuesByChatIdAndLinkId(@Param("chatId") Long chatId, @Param("linkId") Long linkId);
}
