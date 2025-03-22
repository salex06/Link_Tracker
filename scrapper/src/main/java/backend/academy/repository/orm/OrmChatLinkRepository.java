package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChatLink;
import backend.academy.model.orm.OrmChatLinkIdEmbedded;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией о связи чата с ссылкой */
@Repository
public interface OrmChatLinkRepository extends JpaRepository<OrmChatLink, OrmChatLinkIdEmbedded> {}
