package backend.academy.repository;

import backend.academy.model.jdbc.JdbcLink;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JdbcLinkRepository extends CrudRepository<JdbcLink, Long> {
    Page<JdbcLink> findAll(Pageable pageable);

    @Query("SELECT * FROM link WHERE link_value = :url")
    @Transactional
    Optional<JdbcLink> getLinkByUrl(@Param("url") String url);

    @Query("SELECT tg_chat_id FROM tg_chat_link INNER JOIN link ON link_id = id WHERE link_value = :linkValue")
    Set<Long> getChatIdsByUrl(@Param("linkValue") String url);

    @Query(
            "SELECT * FROM link JOIN tg_chat_link ON tg_chat_link.link_id = link.id WHERE link_value = :linkValue AND tg_chat_id = :chatId")
    Optional<JdbcLink> getLinkByUrlAndChatId(@Param("chatId") Long chatId, @Param("linkValue") String url);

    @Query("SELECT link.id, link.link_value, link.last_update FROM link "
            + "INNER JOIN tg_chat_link ON link.id = tg_chat_link.link_id "
            + "WHERE tg_chat_link.tg_chat_id = :chatId")
    List<JdbcLink> getAllLinksByChatId(@Param("chatId") Long chatId);
}
