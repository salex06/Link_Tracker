package backend.academy.repository;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.jdbc.JdbcTgChat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JdbcChatRepository extends CrudRepository<JdbcTgChat, Long> {
    @Query("SELECT EXISTS(SELECT 1 FROM tg_chat chat WHERE chat.chat_id = :chatId)")
    @Transactional
    boolean existsByChatId(@Param("chatId") Long chatId);

    @Query("SELECT * FROM tg_chat chat WHERE chat.chat_id = :chatId")
    @Transactional
    Optional<JdbcTgChat> findByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM tg_chat chat WHERE chat.chat_id = :chatId")
    void deleteByChatId(Long chatId);

    @Query(
            "SELECT id, chat_id FROM tg_chat_link INNER JOIN tg_chat ON tg_chat.chat_id = tg_chat_id WHERE link_id = :linkId")
    @Transactional
    List<JdbcTgChat> getChatsByLink(@Param("linkId") Long linkId);

    @Transactional
    @Query("SELECT tag_value FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId")
    List<String> getTags(@Param("linkId") Long linkId, @Param("chatId") Long chatId);

    @Transactional
    @Query("SELECT filter_value FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId")
    List<String> getFilters(@Param("linkId") Long linkId, @Param("chatId") Long chatId);

    @Query("SELECT * FROM link WHERE link_value = :linkValue")
    Optional<JdbcLink> getLinkByValue(@Param("linkValue") String linkValue);

    @Modifying
    @Transactional
    @Query("INSERT INTO tg_chat_link VALUES (:chatId, :linkId)")
    void saveTheChatLink(@Param("chatId") Long chatId, @Param("linkId") Long linkId);

    @Modifying
    @Transactional
    @Query("DELETE FROM tg_chat_link WHERE tg_chat_id = :chatId AND link_id = :linkId")
    void removeTheChatLink(@Param("chatId") Long chatId, @Param("linkId") Long linkId);

    @Modifying
    @Transactional
    @Query("INSERT INTO chat_link_tags VALUES (:linkId, :tagValue, :chatId)")
    void saveTag(@Param("linkId") Long linkId, @Param("chatId") Long chatId, @Param("tagValue") String tagValue);

    @Modifying
    @Transactional
    @Query("INSERT INTO chat_link_filters VALUES (:linkId, :filterValue, :chatId)")
    void saveFilter(
            @Param("linkId") Long linkId, @Param("chatId") Long chatId, @Param("filterValue") String filterValue);

    @Modifying
    @Transactional
    @Query("DELETE FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId AND tag_value = :tagValue")
    void removeTag(@Param("linkId") Long linkId, @Param("chatId") Long chatId, @Param("tagValue") String tagValue);

    @Modifying
    @Transactional
    @Query("DELETE FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId")
    void removeAllTags(@Param("chatId") Long linkId, @Param("linkId") Long chatId);

    @Modifying
    @Transactional
    @Query(
            "DELETE FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId AND filter_value = :filterValue")
    void removeFilter(
            @Param("linkId") Long linkId, @Param("chatId") Long chatId, @Param("filterValue") String filterValue);

    @Modifying
    @Transactional
    @Query("DELETE FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId")
    void removeAllFilters(@Param("chatId") Long linkId, @Param("linkId") Long chatId);

    @Modifying
    @Transactional
    @Query("INSERT INTO link(link_value) VALUES (:linkValue)")
    JdbcLink saveLink(@Param("linkValue") String linkValue);
}
