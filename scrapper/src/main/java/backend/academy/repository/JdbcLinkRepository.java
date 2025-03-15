package backend.academy.repository;

import backend.academy.model.jdbc.JdbcLink;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JdbcLinkRepository extends CrudRepository<JdbcLink, Long> {
    @Query(
            "SELECT id, link_value, last_update FROM tg_chat_link INNER JOIN link ON link_id = id WHERE tg_chat_id = :chatId")
    @Transactional
    Iterable<JdbcLink> getAllLinksByChatId(@Param("chatId") Long chatId);

    @Query("SELECT * FROM link WHERE link_value = :url")
    @Transactional
    Optional<JdbcLink> getLinkByUrl(@Param("url") String url);

    @Query("SELECT tg_chat_id FROM tg_chat_link INNER JOIN link ON link_id = id WHERE link_value = :linkValue")
    Set<Long> getChatIdsByUrl(@Param("linkValue") String url);

    @Query("\tSELECT (link.id, link.link_value, link.last_update) FROM link\n"
            + "\tINNER JOIN tg_chat_link ON link.id = tg_chat_link.link_id\n"
            + "\tWHERE tg_chat_link.tg_chat_id = :chatId AND link.link_value = :linkValue")
    Optional<JdbcLink> getLinkByUrlAndChatId(@Param("chatId") Long chatId, @Param("linkValue") String url);

    @Query("SELECT tag_value FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId")
    List<String> getTags(@Param("linkId") Long linkId, @Param("chatId") Long chatId);

    @Transactional
    @Query("SELECT filter_value FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId")
    List<String> getFilters(@Param("linkId") Long linkId, @Param("chatId") Long chatId);
}
