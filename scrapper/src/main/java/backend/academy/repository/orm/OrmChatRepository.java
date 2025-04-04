package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrmChatRepository extends JpaRepository<OrmChat, Long> {
    Optional<OrmChat> findByChatId(Long tgChatId);

    boolean existsByChatId(Long tgChatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM OrmChat c WHERE c.chatId = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);
}
