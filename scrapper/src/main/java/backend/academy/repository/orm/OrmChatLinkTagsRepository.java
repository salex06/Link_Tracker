package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChatLinkTags;
import backend.academy.model.orm.OrmChatLinkTagsIdEmbedded;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией из БД о тегах, связанных с чатом и ссылкой */
@Repository
public interface OrmChatLinkTagsRepository extends JpaRepository<OrmChatLinkTags, OrmChatLinkTagsIdEmbedded> {}
